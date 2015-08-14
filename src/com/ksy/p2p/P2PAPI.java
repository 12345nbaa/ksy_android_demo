package com.ksy.p2p;

public class P2PAPI {

	// device
	public native int KSY_Device_Initialize(String P2PHostNamePrimary,
			String P2PHostNameSecondary);

	public native long KSY_Device_Listen(String UID, String token,
			int nTimeout, int mode, int channel_num);

	public native int KSY_Device_Session_Write(long nKSYSessionID,
			String cabBuf, int nBufSize, int channel_id);

	public native int KSY_Device_Session_Read(long nKSYSessionID, byte[] abBuf,
			int nMaxBufSize, int nTimeout, int channel_id);

	public native int KSY_Device_Session_Close(long nKSYSessionID);

	public native int KSY_Device_Session_Type_Check(long nKSYSessionID);

	public native int KSY_Device_Session_State_Check(long nKSYSessionID);

	public native int KSY_Device_DeInitialize();

	// client
	public native int KSY_Client_Initialize(String P2PHostNamePrimary,
			String P2PHostNameSecondary);

	public native long KSY_Client_Connect(String UID, String token,
			int nTimeout, int channel_num);

	public native int KSY_Client_Session_Write(long nKSYSessionID,
			String cabBuf, int nBufSize, int channel_id);

	public native int KSY_Client_Session_Read(long nKSYSessionID, byte[] abBuf,
			int nMaxBufSize, int nTimeout, int channel_id);

	public native int KSY_Client_Session_Close(long nKSYSessionID);

	public native int KSY_Client_Session_Type_Check(long nKSYSessionID);

	public native int KSY_Client_Session_State_Check(long nKSYSessionID);

	public native int KSY_Client_DeInitialize();
}
