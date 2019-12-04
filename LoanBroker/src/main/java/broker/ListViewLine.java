package broker;


import bank.model.BankInterestReply;
import loanclient.model.LoanRequest;
/**
 * This class is an item/line for a ListViewLine. It makes it possible to put both BankInterestRequest and BankInterestReply object in one item in a ListViewLine.
 */
class ListViewLine {

	private LoanRequest loanRequest;
	private BankInterestReply bankReply;

	public ListViewLine(LoanRequest loanRequest) {
		setLoanRequest(loanRequest);
		setBankReply(null);
	}

	public LoanRequest getBankRequest() {
		return loanRequest;
	}

	private void setLoanRequest(LoanRequest loanRequest) {
		this.loanRequest = loanRequest;
	}


	public void setBankReply(BankInterestReply bankReply) {
		this.bankReply = bankReply;
	}

    /**
     * This method defines how one line is shown in the ListViewLine.
     * @return
     *  a) if BankInterestReply is null, then this item will be shown as "loanRequest.toString ---> waiting for loan reply..."
     *  b) if BankInterestReply is not null, then this item will be shown as "loanRequest.toString ---> loanReply.toString"
     */
	@Override
	public String toString() {
	   return loanRequest.toString() + "  --->  " + ((bankReply !=null)? bankReply.toString():"waiting for loan reply...");
	}

}
