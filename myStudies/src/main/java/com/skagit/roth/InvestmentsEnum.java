package com.skagit.roth;

import java.util.TreeMap;

public enum InvestmentsEnum {
    CPTL_GN_DSTRBTNS("Capital Gain Distributions"), //
    TX_EXMPT_INCM("Tax-Exempt Income"), //
    NT_SHRT_TRM("Net Short-Term"), //
    NT_LNG_TRM("Net Long-Term"), //
    FRGN_CRRNCY_GN_LSS("Foreign Currency Gain/Loss"), //
    NN_QULFD_DVDNDS("- Non-Qualified Dividends"), //
    QULFD_DVDNDS("- Qualified Dividends"), //
    INVSTMNT_EXPNSS("Investment Expenses"), //
    FRGN_TX_PD("Foreign Tax Paid"), //
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
