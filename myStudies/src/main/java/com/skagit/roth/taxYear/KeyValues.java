package com.skagit.roth.taxYear;

import java.util.EnumSet;

import com.skagit.roth.rothCalculator.InvestmentsEnum;

public class KeyValues {
    /**
     * <pre>
     CPTL_GN_DSTRBTNS("Capital Gain Distributions"), //
     TX_EXMPT_INCM("Tax-Exempt Income"), //
     NT_SHRT_TRM("Net Short-Term"), //
     NT_LNG_TRM("Net Long-Term"), //
     FRGN_CRRNCY_GN_LSS("Foreign Currency Gain/Loss"), //
     NN_QULFD_DVDNDS("- Non-Qualified Dividends"), //
     QULFD_DVDNDS("- Qualified Dividends"), //
     INVSTMNT_EXPNSS("Investment Expenses"), //
     FRGN_TX_PD("Foreign Tax Paid"), //
     * </pre>
     */
    public final EnumSet<InvestmentsEnum> _Ordinary = EnumSet.of( //
	    InvestmentsEnum.NT_SHRT_TRM, //
	    InvestmentsEnum.NN_QULFD_DVDNDS, //
	    InvestmentsEnum.FRGN_CRRNCY_GN_LSS//
    );
    public final EnumSet<InvestmentsEnum> _NegativeOrdinary = EnumSet.of( //
	    InvestmentsEnum.INVSTMNT_EXPNSS, //
	    InvestmentsEnum.FRGN_TX_PD //
    );
    /**
     * <pre>
     *  For Calculating SSA Taxes:
     *  https://www.kiplinger.com/retirement/social-security/604321/taxes-on-social-security-benefits
     * </pre>
     */
    /**
     * <pre>
     *  For Calculating AGI, MAGI, etc.:
     *  https://www.healthreformbeyondthebasics.org/key-facts-income-definitions-for-marketplace-and-medicaid-coverage/
     * </pre>
     */
    public final TaxYear _taxYear;

    /** These are pre-optional incomes. */
    public double _outsideIncome;
    public double _agi;
    public double _combinedIncome;
    public double _nonTaxableInterest;
    public double _ssaBenefits;
    public double _longCf;
    public double _shortCf;
    public double _longCg;
    public double _shortCg;

    @SuppressWarnings("unused")
    public KeyValues(final TaxYear taxYear) {
	_taxYear = taxYear;
	final double inflationFactor = _taxYear._inflationFactor;
	final double investmentsFactor = _taxYear._investmentsFactor;
	// TODO Auto-generated constructor stub
    }

}
