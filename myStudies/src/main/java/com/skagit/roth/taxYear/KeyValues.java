package com.skagit.roth.taxYear;

import java.util.EnumSet;

import com.skagit.roth.InvestmentsEnum;

public class KeyValues {
    public final EnumSet<InvestmentsEnum> _Ordinary = EnumSet.of( //
	    InvestmentsEnum.ORDNRY_DIV, //
	    InvestmentsEnum.CG_DISTS, //
	    InvestmentsEnum.INTRST_INC, //
	    InvestmentsEnum.MSC_INC, //
	    InvestmentsEnum.ORIG_ISS_DISC, //
	    InvestmentsEnum.FRGN_CRRNCY_GN, //
	    InvestmentsEnum.TAX_EXMPT_INCM, //
	    InvestmentsEnum.NET_SHORT_TERM, //
	    InvestmentsEnum.NET_LONG_TERM, //
	    InvestmentsEnum.RPRTBL_BND_PRMM, //
	    InvestmentsEnum.RLZD_ACC_MRKT_DSC_INC, //
	    InvestmentsEnum.ORD_INC_OR_LSS, //
	    InvestmentsEnum.RPRTBL_ACQ_PREM, //
	    InvestmentsEnum.MRGN_INT_PD, //
	    InvestmentsEnum.OPTN_SLS, //
	    InvestmentsEnum.RTRN_OF_PRNCPL //
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

    public KeyValues(final TaxYear taxYear) {
	_taxYear = taxYear;
	final double inflationFactor = _taxYear._inflationFactor;
	final double investmentsFactor = _taxYear._investmentsFactor;
	// TODO Auto-generated constructor stub
    }

}
