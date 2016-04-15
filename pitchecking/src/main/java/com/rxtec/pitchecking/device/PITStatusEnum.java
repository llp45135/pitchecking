package com.rxtec.pitchecking.device;

public enum PITStatusEnum {
	 DefaultStatus(0)
	,ReadQR(1)
	,QRChecked(2)
	,FirstDoorOpened(3)
	,IDCardReaded(4)
	,FaceChecked(5)
	,FaceCheckedFailed(6)
	,SecondDoorOpened(7)
	,Exception(8)
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

    
    private PITStatusEnum(int value) {
        this.value = value;
    }


}
