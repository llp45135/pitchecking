package com.rxtec.pitchecking;

public enum ScreenCmdEnum {
	
	showFaceDefaultContent(1),
	ShowBeginCheckFaceContent(2), 
	ShowFaceCheckPass(3), 
	ShowFaceCheckFailed(4),
	showIDCardImage(5), 
	ShowTicketDefault(6), 
	ShowTicketVerifyIDFail(7), 
	ShowTicketVerifyStationRuleFail(8),
	ShowTicketVerifyWaitInput(9),
	ShowTicketVerifySucc(10),
	ShowTicketVerifyTrainDateRuleFail(11),
	ShowFaceDisplayFromTK(12),
	ShowQRDeviceException(-1),
	ShowIDDeviceException(-2),
	ShowVersionFault(-3),
	ShowCamOpenException(-4),
	ShowStopCheckFault(-5)
	{
		@Override
		public boolean isRest() {
			return true;
		}
	},
	SUN(0) {
		@Override
		public boolean isRest() {
			return true;
		}
	};

    private int value;

    public int getValue() {
        return value;
    }

    public boolean isRest() {
        return false;
    }

    
    private ScreenCmdEnum(int value) {
        this.value = value;
    }


}
