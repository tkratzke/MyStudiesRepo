package com.skagit.roth.taxYear;

import java.util.EnumSet;

import com.skagit.roth.rothCalculator.InvestmentsEnum;

public class KeyValues {
    public final EnumSet<InvestmentsEnum> _Ordinary = EnumSet.of( //
	    InvestmentsEnum.NT_SHRT_TRM, //
	    InvestmentsEnum.NN_QULFD_DVDNDS//
    );
    public final EnumSet<InvestmentsEnum> _NegativeOrdinary = EnumSet.of( //
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
    public final double _ttlSocialSecurityBenefits;
    public double _agi;
    public double _nonTaxableIncome;
    public double _taxableIncome;
    public double _combinedIncome;
    public double _magi;

    public KeyValues(final TaxYear taxYear, final double ttlSocialSecurityBenefits) {
	_ttlSocialSecurityBenefits = ttlSocialSecurityBenefits;
	_agi = _taxableIncome = _combinedIncome = 0d;
    }

}
