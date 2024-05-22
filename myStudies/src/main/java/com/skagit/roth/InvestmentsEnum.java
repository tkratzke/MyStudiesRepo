package com.skagit.roth;

import java.util.TreeMap;

public enum InvestmentsEnum {
    ORDNRY_DIV("- Ordinary Dividends"), //
    CG_DISTS("- Capital Gain Distributions"), //
    INTRST_INC("Interest Income"), //
    MSC_INC("Miscellaneous Income"), //
    ORIG_ISS_DISC("Original Issue Discount"), //
    FRGN_CRRNCY_GN("Foreign Currency Gain/Loss"), //
    TAX_EXMPT_INCM("Tax-Exempt Income"), //
    NET_SHORT_TERM("Net Short-Term"), //
    NET_LONG_TERM("Net Long-Term"), //
    RPRTBL_BND_PRMM("Reportable Bond Premium"), //
    RLZD_ACC_MRKT_DSC_INC("Realized Accrued Market Discount Income"), //
    ORD_INC_OR_LSS("Ordinary Income or Loss **"), //
    RPRTBL_ACQ_PREM("Reportable Acquisition Premium"), //
    MRGN_INT_PD("Margin Interest Paid"), //
    OPTN_SLS("Option Sales"), //
    RTRN_OF_PRNCPL("Return of Principal"),
//
    NON_QULFD_DVDND("- Non-Qualified Dividends"), //
    QULFD_DVDND("- Qualified Dividends"), //
    SCTN_897_DVDND("- Section 897 Ordinary Dividends"), //
    SCTN_199A_DVDND("- Section 199A Dividends"), //
    UNRCPTRD_1250_CGS("- Unrecaptured Section 1250 Capital Gains"), //
    SCTN_1202_DVDND("- Section 1202 Capital Gains"), //
    _28PR_CNT_CGS("- 28% Rate Capital Gains"), //
    SCTN_897_CGS("- Section 897 Capital Gain"), //
    _15PR_CNT_CGS("- 15% Rate Capital Gains"), //
    NON_DVDND_DSTRBTNS("Nondividend Distributions"), //
    INVSTMNT_EXPNSS("Investment Expenses"), //
    FRGN_TX_PD("Foreign Tax Paid"), //
    CSH_LIQ_DSTNS("Cash Liquidation Distributions"), //
    NON_CSH_LIQ_DSTNS("Non-cash Liquidation Distributions"), //
    TTL_TX_EXMPT_INTRST_DVDNDS("Total Tax Exempt Interest Dividends"), //
    ;

    final public static InvestmentsEnum[] _Values = InvestmentsEnum.values();

    final String _originalString;

    InvestmentsEnum(final String originalString) {
	_originalString = originalString;
    }

    public static final TreeMap<String, InvestmentsEnum> _ReverseMap = new TreeMap<>() {
	private static final long serialVersionUID = 1L;
	{
	    for (final InvestmentsEnum e : _Values) {
		put(e._originalString, e);
	    }
	}
    };
}
