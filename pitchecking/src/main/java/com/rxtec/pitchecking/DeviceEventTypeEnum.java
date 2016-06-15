package com.rxtec.pitchecking;

public enum DeviceEventTypeEnum {
	
	FindIDCard(1),ReadIDCard(2){
        @Override
        public boolean isRest() {
            return true;
        }
    },
    ReadQRCode(0) {
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

    
    private DeviceEventTypeEnum(int value) {
        this.value = value;
    }


}
