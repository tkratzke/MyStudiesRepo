package com.skagit.roth;

public enum InvestmentsLine {
    ORD_DIV("- Ordinary Dividends"), //
    CG_DISTS("- Capital Gain Distributions"), //
    INT_INC("Interest Income"), //
    MSC_INC("Miscellaneous Income"), //
    ORIG_ISS_DISC("Original Issue Discount"), //
    FRGN_CRRNCY_GN("Foreign Currency Gain/Loss"), //
    TAX_EXEMPT("Tax-Exempt Income"), //
    NET_SHORT_TERM("Net Short-Term"), //
    NET_LONG_TERM("Net Long-Term"), //
    RPRTBL_BND_PRMM("Reportable Bond Premium"), //
    RLZD_ACC_MRKT_DSC_INC("Realized Accrued Market Discount Income"), //
    ORD_INC("Ordinary Income or Loss **"), //
    RPRTBL_ACQ_PREM("Reportable Acquisition Premium"), //
    MRGN_INT_PD("Margin Interest Paid"), //
    OPTN_SLS("Option Sales"), //
    RTRN_OF_PRNCPL("Return of Principal");

    final String _originalString;

    InvestmentsLine(final String originalString) {
	_originalString = originalString;
    }

}
