package com.nibss.tqs.billing;

import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.util.Utility;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by eoriarewo on 8/18/2016.
 */
public interface BillingProvider {

    RoundingMode ROUNDING_MODE = RoundingMode.HALF_DOWN;

    ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>() {
        @Override
        public DateFormat get() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    enum BillingPeriod {
        WEEKLY, BACKLOGS, MONTHLY, PERIOD
    }

    Path getBillingZipFile(List<? extends Serializable> transactions, BillingPeriod billingPeriod) throws  RuntimeException, IOException;
    void generatePartyReports(List<? extends Serializable> transactions, BillingPeriod billingPeriod);

    void cleanUp();


    default BigDecimal getShare(BigDecimal partyShare, BigDecimal fee, boolean isPercentage) {
        BigDecimal share;
        if (Utility.isEmptyBigDecimal(partyShare))
            return BigDecimal.ZERO;

        if (isPercentage)
            share  =  partyShare.multiply(fee);
        else
            share = partyShare;

        return share.setScale(2, ROUNDING_MODE);
    }
}
