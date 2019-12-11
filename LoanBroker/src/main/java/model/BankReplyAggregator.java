package model;

import bank.model.BankInterestReply;

import java.util.ArrayList;
import java.util.List;

public class BankReplyAggregator {
    private List<BankInterestReply> replyList;
    private int nrOfExpectedReplies;
    public BankReplyAggregator(int nrOfExpectedReplies) {
        this.nrOfExpectedReplies = nrOfExpectedReplies;
        replyList = new ArrayList<>();
    }
    public void addReply(BankInterestReply reply){
        replyList.add(reply);
    }
    public boolean finish(){
        if (replyList.size() == nrOfExpectedReplies)
            return true;
        return false;
    }

    public BankInterestReply findBestReply()
    {
       double min = replyList.iterator().next().getInterest();
        BankInterestReply bestReply = null;
        for(BankInterestReply bankInterestReply: replyList){
            if(bankInterestReply.getInterest() < min){
                min = bankInterestReply.getInterest();
                bestReply = bankInterestReply;
            }
        }
        return bestReply;
    }
}
