package com.rxtec.pitchecking;

public enum ScreenCmdEnum {
	
	showDefaultContent(1),ShowBeginCheckFaceContent(2),ShowFaceCheckPass(3),ShowFaceCheckFailed(4){
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
