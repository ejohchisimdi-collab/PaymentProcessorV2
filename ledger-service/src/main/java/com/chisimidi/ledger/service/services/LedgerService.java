package com.chisimidi.ledger.service.services;

import com.chisimdi.payment.events.CaptureEvent;
import com.chisimdi.payment.events.LedgerPaymentEvent;
import com.chisimdi.payment.events.RefundCompletedEvent;
import com.chisimidi.ledger.service.mappers.LedgerEntriesMapper;
import com.chisimidi.ledger.service.mappers.LedgerMapper;
import com.chisimidi.ledger.service.mappers.RefundsMapper;
import com.chisimidi.ledger.service.mappers.SplitMapper;
import com.chisimidi.ledger.service.models.*;
import com.chisimidi.ledger.service.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class LedgerService {
    private LedgerEntriesRepository ledgerEntriesRepository;
    private LedgerRepository ledgerRepository;
    private RefundsRepository refundsRepository;
    private SplitRepository splitRepository;
    private LedgerMapper ledgerMapper;
    private LedgerEntriesMapper ledgerEntriesMapper;
    private SplitMapper splitMapper;
    private RefundsMapper refundsMapper;
    private KafkaTemplate<String,Object>kafkaTemplate;
    private EventsIdempotencyRepository idempotencyRepository;

    @KafkaListener(topics = "payment-captured")
    public void createSplit(CaptureEvent captureEvent){
        EventIdempotency eventIdempotency=idempotencyRepository.findById(captureEvent.getId()).orElse(null);
        if(eventIdempotency!=null){
            return;
        }
        Ledger ledger=new Ledger();
        ledger.setPaymentId(captureEvent.getPaymentId());
        ledger.setMerchantId(captureEvent.getMerchantId());
        Split split=Split.builder().
                ledger(ledger).
                createdAt(Instant.now()).
                accountFrom(captureEvent.getCustomerAccount()).
                accountTo(captureEvent.getMerchantAccount()).entryReason(EntryReason.PAYMENT_CAPTURE)
                .firstDebitEntry(EntryType.EXTERNAL_PSP_RECEIVABLE)
                .firstDebitAmount(captureEvent.getTotalAmount()).firstCreditAmount(captureEvent.getTotalAmount())
                .firstCreditEntry(EntryType.MERCHANT_PENDING_BALANCE)
                .firstCreditAmount(captureEvent.getTotalAmount().subtract(captureEvent.getPlatformFee()))
                .secondCreditEntry(EntryType.PLATFORM_FEE_REVENUE)
                .secondCreditAmount(captureEvent.getPlatformFee())
                .maturingDate(Instant.now().plus(Duration.ofDays(3))).done(false).build();

        ledgerRepository.save(ledger);
        splitRepository.save(split);
        EventIdempotency idempotency=new EventIdempotency();
        idempotency.setContext("Create split event");
        idempotency.setId(captureEvent.getId());
        idempotencyRepository.save(idempotency);



    }


    public List<LedgerEntries> createMaturity(){
        List<Split>splits=splitRepository.findByDoneAndMaturingDateBefore(false,Instant.now());
        List<LedgerEntries>ledgerEntries1=new ArrayList<>();
       List<Split>splits1=new ArrayList<>();
        for(Split split:splits){
            LedgerEntries ledgerEntries=LedgerEntries.builder().
                    creditEntry(EntryType.MERCHANT_AVAILABLE_BALANCE)
                    .creditAmount(split.getFirstCreditAmount()).debitEntry(EntryType.MERCHANT_PENDING_BALANCE)
                    .debitAmount(split.getFirstCreditAmount()).entryReason(EntryReason.PAYMENT_MATURITY).ledger(split.getLedger())
                    .accountFrom(split.getAccountFrom()).accountTo(split.getAccountTo()).createdAt(Instant.now()).build();
            ledgerEntries1.add(ledgerEntries);
            split.setDone(true);
            splits1.add(split);
        }
        ledgerEntriesRepository.saveAll(ledgerEntries1);
        splitRepository.saveAll(splits1);
        return ledgerEntries1;
    }

    public void initiateBankTransfer(List<LedgerEntries>ledgerEntries){
        List<LedgerEntries>ledgerEntries2=new ArrayList<>();
        for(LedgerEntries entries:ledgerEntries){
            LedgerEntries ledgerEntries1= LedgerEntries.builder().
                    debitEntry(EntryType.MERCHANT_AVAILABLE_BALANCE)
                    .debitAmount(entries.getCreditAmount())
                    .creditEntry(EntryType.MERCHANT_PAYOUT_IN_TRANSIT)
                    .creditAmount(entries.getCreditAmount()).
                    accountFrom(entries.getAccountFrom()).
                    ledger(entries.getLedger()).
                    accountTo(entries.getAccountTo()).entryReason(EntryReason.PAYOUT_INITIATION)
                    .createdAt(Instant.now()).build();
            ledgerEntries2.add(ledgerEntries1);
            LedgerPaymentEvent paymentEvent=new LedgerPaymentEvent();
            paymentEvent.setLedgerId(ledgerEntries1.getLedger().getId());
            paymentEvent.setAccountFrom(ledgerEntries1.getAccountFrom());
            paymentEvent.setAccountTo(ledgerEntries1.getAccountTo());
            paymentEvent.setAmount(ledgerEntries1.getCreditAmount());
            paymentEvent.setId(UUID.randomUUID().toString());
            kafkaTemplate.send("payout-initiated",paymentEvent);

        }
        ledgerEntriesRepository.saveAll(ledgerEntries2);

    }
    @KafkaListener(topics = "payout-completed")
    public void completePayout(LedgerPaymentEvent paymentEvent) {
        EventIdempotency eventIdempotency=idempotencyRepository.findById(paymentEvent.getId()).orElse(null);
        if(eventIdempotency!=null){
            return;
        }
        Ledger ledger = ledgerRepository.findById(paymentEvent.getLedgerId()).orElse(null);
        if (ledger == null) {
            return;
        }
        LedgerEntries ledgerEntries = LedgerEntries.builder().
                debitEntry(EntryType.MERCHANT_AVAILABLE_BALANCE).
                debitAmount(paymentEvent.getAmount()).
                creditEntry(EntryType.MERCHANT_PAYOUT_IN_TRANSIT)
                .creditAmount(paymentEvent.getAmount()).entryReason(EntryReason.PAYOUT_COMPLETION).ledger(ledger).
                accountTo(paymentEvent.getAccountTo())
                .accountFrom(paymentEvent.getAccountFrom()).createdAt(Instant.now()).build();

        ledgerEntriesRepository.save(ledgerEntries);
        EventIdempotency idempotency=new EventIdempotency();
        idempotency.setContext("Payout completed Event");
        idempotency.setId(paymentEvent.getId());
        idempotencyRepository.save(idempotency);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void processLedger(){
        List<LedgerEntries>ledgerEntries=createMaturity();
        initiateBankTransfer(ledgerEntries);
    }

    @KafkaListener(topics = "refund-completed")
    public void refundCompletedLedger(RefundCompletedEvent completedEvent){
        EventIdempotency eventIdempotency=idempotencyRepository.findById(completedEvent.getId()).orElse(null);
        if(eventIdempotency!=null){
            return;
        }

        Ledger ledger=ledgerRepository.findByPaymentId(completedEvent.getPaymentId());
        Refunds refunds= Refunds.builder().accountFrom(completedEvent.getMerchantToken()).accountTo(completedEvent.getCustomerAccountToken()).createdAt(Instant.now()).entryReason(EntryReason.REFUNDS_INITIATED)
                .firstCreditEntry(EntryType.MERCHANT_PENDING_BALANCE).ledger(ledger)
                .firstCreditAmount(completedEvent.getAmount()).firstDebitEntry(EntryType.EXTERNAL_PSP_RECEIVABLE)
                .firstDebitAmount(completedEvent.getAmount()).build();

        refundsRepository.save(refunds);
        EventIdempotency idempotency=new EventIdempotency();
        idempotency.setContext("Refund completed event");
        idempotency.setId(completedEvent.getId());
        idempotencyRepository.save(idempotency);
    }

    public LedgerDTO findAllLedgersByPayment(int merchantId,int paymentId){
        Ledger ledger= ledgerRepository.findByMerchantIdAndPaymentId(merchantId,paymentId);
       return ledgerMapper.toLedgerDTO(ledger);
    }

    public List<LedgerEntriesDTO>findAllLedgerEntriesByPayment(int merchantId,int paymentId,int pageNumber,int size){
        Page<LedgerEntries>ledgerEntries=ledgerEntriesRepository.findByLedgerPaymentIdAndLedgerMerchantId(paymentId,merchantId, PageRequest.of(pageNumber,size));
        return ledgerEntriesMapper.toLedgerEntriesDTO(ledgerEntries.getContent());
    }

    public SplitDTO findSplitByPayment(int paymentId,int merchantId){
        Split split=splitRepository.findByLedgerPaymentIdAndLedgerMerchantId(paymentId, merchantId);
        return splitMapper.toSplitDTO(split);
    }
    public RefundsDTO findRefundByPayment(int paymentId,int merchantId){
        Refunds refunds=refundsRepository.findByLedgerPaymentIdAndLedgerMerchantId(paymentId,merchantId);
        return refundsMapper.toRefundsDTO(refunds);
    }

}

