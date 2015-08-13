package com.ksy.p2p;

public class P2PAPI {

	// device
	public native int KSY_Device_Initialize(String P2PHostNamePrimary,
			String P2PHostNameSecondary);

	public native int KSY_Device_Listen(String UID, String token, int nTimeout,
			int mode, int channel_num);

	public native int KSY_Device_Session_Write(int nKSYSessionID,
			String cabBuf, int nBufSize, int channel_id);

	public native int KSY_Device_Session_Read(int nKSYSessionID, byte[] abBuf,
			int nMaxBufSize, int nTimeout, int channel_id);

	public native int KSY_Device_Session_Close(int nKSYSessionID);

	public native int KSY_Device_Session_Type_Check(int nKSYSessionID);

	public native int KSY_Device_Session_State_Check(int nKSYSessionID);

	public native int KSY_Device_DeInitialize();

	// client
	public native int KSY_Client_Initialize(String P2PHostNamePrimary,
			String P2PHostNameSecondary);

	public native int KSY_Client_Connect(String UID, String token,
			int nTimeout, int channel_num);

	public native int KSY_Client_Session_Write(int nKSYSessionID,
			String cabBuf, int nBufSize, int channel_id);

	public native int KSY_Client_Session_Read(int nKSYSessionID, byte[] abBuf,
			int nMaxBufSize, int nTimeout, int channel_id);

	public native int KSY_Client_Session_Close(int nKSYSessionID);

	public native int KSY_Client_Session_Type_Check(int nKSYSessionID);

	public native int KSY_Client_Session_State_Check(int nKSYSessionID);

	public native int KSY_Client_DeInitialize();
}
