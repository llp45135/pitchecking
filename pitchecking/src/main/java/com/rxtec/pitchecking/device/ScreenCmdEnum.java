package com.rxtec.pitchecking.device;

public enum ScreenCmdEnum {
	
	ShowFaceCheckResult(1),showDefaultContent(2),ShowBeginCheckFaceContent(3){
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
