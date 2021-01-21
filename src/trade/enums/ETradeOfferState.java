package trade.enums;

public enum ETradeOfferState {

    Invalid,
    Active,            // This trade offer has been sent, neither party has acted on it yet.
    Accepted,          // The trade offer was accepted by the recipient and items were exchanged.
    Countered,         // The recipient made a counter offer
    Expired,           // The trade offer was not accepted before the expiration date
    Canceled,          // The sender cancelled the offer
    Declined,          // The recipient declined the offer
    InvalidItems,     // Some of the items in the offer are no longer available (indicated by the missing flag in the output)
    CreatedNeedsConfirmation, // The offer hasn't been sent yet and is awaiting further confirmation
    CanceledBySecondFactor, // Either party canceled the offer via email/mobile confirmation
    InEscrow;      // The trade has been placed on hold

    public static int getIntValue(ETradeOfferState state){
        return 1 + state.ordinal();
    }

    public static ETradeOfferState getStringValue(int intValue){
        for(ETradeOfferState state : ETradeOfferState.values()) {
            if(ETradeOfferState.getIntValue(state) == intValue) {
                return state;
            };
        }
        return null;
    }
}
