package com.ksy.p2p;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

/**
 * 
 * @author LIXIAOPENG
 * 
 */
public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "lixp";
	private Button mButtonServer;
	private Button mButtonClient;
	private EditText mEditTextContent;
	private Button mButtonSend;

	private ListView mListView;
	private KsyMsgAdapter mAdapter;// 消息视图的Adapter
	private List<KsyMsgEntity> mDataArrays = new ArrayList<KsyMsgEntity>();// 消息对象数组

	long clientsid;
	long serverSid;
	P2PAPI clientApi = new P2PAPI();
	P2PAPI serverApi = new P2PAPI();
	private int server = 0;
	private int server_int = 0;

	private int client = 0;
	private int client_int = 0;

	private static final String HOST_IP = "115.231.96.88";

	private static final int TIME_OUT = 5;
	private static final int CONNECT_SUCCEED = 11;
	private static final int CLIENT_SEND = 12;
	private static final int SERVER_SEND = 13;
	private static final int INIT_ERROR = 14;
	private static final int READ_FAILED = 15;
	private static final int READ_BLOCKED = 16;
	private static final int READ_UNEXCEPTED = 17;
	private static final int WRITE_FAILED = 18;
	private static final int WRITE_BLOCKED = 19;
	private static final int WRITE_UNEXCEPTED = 20;
	private static final int SESSION_CHECK = 21;
	
	private static final int CONNECT_FAILED = 22;
	private static final int CONNECT_TIMEOUT = 23;

	private List<String> mServerList = new LinkedList<String>();
	private List<String> mClientList = new LinkedList<String>();
	String contentUtf = null;
	String serverString = null;
	String clientString = null;

	String serverData = null;
	String clientData = null;

	private Handler mHandler;
	private HandlerThread mHandlerThread;

	private boolean mClientRunning;
	private boolean mServerRunning;

	private Spinner serverSpinner;
	private Spinner clientSpinner;
	private ArrayAdapter serverAdapter;
	private ArrayAdapter clientAdapter;
	private String serverUID = null;
	private String clientUID = null;

	byte[] byteServer;
	byte[] byteClient;

	static {
		System.loadLibrary("KSYP2PAPI");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		initHandler();

	}

	/**
	 * init
	 */
	private void initHandler() {
		mHandlerThread = new HandlerThread("p2p", 5);
		mHandlerThread.start();

		mHandler = new Handler(mHandlerThread.getLooper());
	}

	/**
	 * client thread
	 */
	Runnable clientRunnable = new Runnable() {
		@Override
		public void run() {
			while (mClientRunning) {

				if (client == 1) {// 点击client按钮后

					if (client_int == 0) {// 初始化只走一次
						client_int = 1;

						try {
							int clientinit = clientApi.KSY_Client_Initialize(
									HOST_IP, "");

							if (clientinit < 0) {
								Message message = Message.obtain();
								message.what = INIT_ERROR;
								handler.sendMessage(message);
							}

						} catch (Exception e) {
							Log.e(TAG, "clientRunnable 111 e =" + e);
						}

						if (clientUID != null) {

							try {
								clientsid = clientApi
										.KSY_Client_Connect(
												clientUID,
												"expire=1710333230&public=1&nonce=12341234&accesskey=2HITWMQXL2VBB3XMAEHQ&signature=1wgD2F56CDUizTp0%2fj3DJ%2fasSsY%3d",
												TIME_OUT, 2);
//								Log.d(TAG, "clientRunnable clientsid =" + clientsid);

							} catch (Exception e) {
								Log.e(TAG, "clientRunnable 222 e =" + e);
							}

						} else {
							Log.e(TAG, "clientRunnable clientUID = null");
						}

						try {
							int session_type = clientApi
									.KSY_Client_Session_Type_Check(clientsid);
							Log.d(TAG, "clientRunnable session_type ="
									+ session_type);

							if (session_type < 0) {

								Message message = Message.obtain();
								message.what = SESSION_CHECK;
								handler.sendMessage(message);
							}

						} catch (Exception e) {
							Log.e(TAG, "clientRunnable 444 e =" + e);
						}
					}

					try {

						byteClient = new byte[1024 * 2];

						int size = clientApi.KSY_Client_Session_Read(clientsid,
								byteClient, 1024 * 2, 300, 0);

//						 Log.d(TAG, "clientRunnable 11 size=" + size);

						if (size > 0) {

							clientData = new String(byteClient, 0, size, "UTF-8");
							
							/*if (clientData != null) {
								String client1 = StrToBinstr(clientData);
								Log.d(TAG, "clientRunnable read client1= " + client1);
								Log.d(TAG, "clientRunnable read clientData="
										+ clientData +":::::=" + clientData.length());
							}*/

							Message message = Message.obtain();
							message.what = CLIENT_SEND;
							handler.sendMessage(message);

							byteClient = null;

						} else if (size == -1) {
							Message message = Message.obtain();
							message.what = READ_FAILED;
							handler.sendMessage(message);

						} else if (size == -2) {
							// TODO
						   Message message = Message.obtain(); 
						   message.what = READ_BLOCKED; 
						   handler.sendMessage(message);

						} else if (size == -3) {
							Message message = Message.obtain();
							message.what = READ_UNEXCEPTED;
							handler.sendMessage(message);
						}

					} catch (Exception e) {
						Log.e(TAG, "clientRunnable 555 e =" + e);
					}

					if (mClientList.size() > 0) {
						for (int j = 0; j < mClientList.size(); j++) {
							clientString = mClientList.get(j);
							/*
							if (clientString != null) {
								String client2 = StrToBinstr(clientString);
								Log.d(TAG, "clientRunnable Write client2= " + client2);

								Log.d(TAG, "clientRunnable Write clientString ="
										+ clientString);
							}*/

							if (clientString != null) {
								try {
									clientString = new String(clientString.getBytes("UTF-8"));
									
									int write = clientApi
											.KSY_Client_Session_Write(
													clientsid, clientString,
													clientString.getBytes().length, 0);
//                                    Log.e(TAG, "clientRunnable  write =" + write + ">>clientString=" + clientString + "<>><=" + clientString.getBytes().length);
									if (write == -1) {
										Message message = Message.obtain();
										message.what = WRITE_FAILED;
										handler.sendMessage(message);

									} else if (write == -2) {
										Message message = Message.obtain();
										message.what = WRITE_BLOCKED;
										handler.sendMessage(message);

									} else if (write == -3) {
										Message message = Message.obtain();
										message.what = WRITE_UNEXCEPTED;
										handler.sendMessage(message);
									}

								} catch (Exception e) {
									Log.e(TAG, "clientRunnable 666 e =" + e);
								}

							} else {
								Log.e(TAG,
										"clientRunnable  clientString = null ");
							}

							clientString = null;
						}
					}

					mClientList.clear(); // 清空list

				} else {
					// 没有点击前初始
					Log.d(TAG, "........clientRunnable  client =" + client);
				}
			}
		}
	};

	/**
	 * server thread
	 */
	Runnable serverRunnable = new Runnable() {
		@Override
		public void run() {

			while (mServerRunning) {

				if (server == 1) { // 点击server后
					if (server_int == 0) {// 初始化只走一次
						server_int = 1;

						try {
							int init = serverApi.KSY_Device_Initialize(HOST_IP,
									"");

							if (init < 0) {
								Message message = Message.obtain();
								message.what = INIT_ERROR;
								handler.sendMessage(message);
							}

							Log.d(TAG, "serverRunnable serverUID =" + serverUID
									+ ">>>init ===" + init);

						} catch (Exception e) {
							Log.e(TAG, "serverRunnable 111111 e =" + e);
						}

						// "000001111222233334444555566667777@xiaoyi"
						if (serverUID != null) {
							try {
								serverSid = serverApi
										.KSY_Device_Listen(
												serverUID,
												"expire=1710333230&public=1&nonce=12341234&accesskey=2HITWMQXL2VBB3XMAEHQ&signature=1wgD2F56CDUizTp0%2fj3DJ%2fasSsY%3d",
												TIME_OUT, 0, 2);
								Log.d(TAG, "serverRunnable  serverSid ="
										+ serverSid);

								// TODO
								if(serverSid >= 0) {
									Message message = Message.obtain();
									message.what = CONNECT_SUCCEED;
									handler.sendMessage(message);
									
								} else if(serverSid == -1) {
									Message message = Message.obtain();
									message.what = CONNECT_FAILED;
									handler.sendMessage(message);
									
								} else if (serverSid == -2) {
									Message message = Message.obtain();
									message.what = CONNECT_TIMEOUT;
									handler.sendMessage(message);
									
								}

							} catch (Exception e) {
								Log.e(TAG, "serverRunnable 222222 e =" + e);
							}

						} else {
							Log.e(TAG, "serverRunnable  serverUID = null");
						}
					}

					try {

						byteServer = new byte[1024 * 2];

						int sizee = serverApi.KSY_Device_Session_Read(
								serverSid, byteServer, 1024 * 2, 300, 0);
//						 Log.d(TAG, "serverRunnable 11 sizee ===" + sizee);

						if (sizee > 0) {
							serverData = new String(byteServer, 0, sizee, "UTF-8");
							
							/*if (serverData != null) {
								String server1 = StrToBinstr(serverData);
								Log.d(TAG, "serverRunnable read server1= " + server1);

								Log.d(TAG, "serverRunnable read serverData="
										+ serverData + "<<>>?=" + serverData.length());
							}*/

							Message message = Message.obtain();
							message.what = SERVER_SEND;
							handler.sendMessage(message);

							byteServer = null;

						} else if (sizee == -1) {
							Message message = Message.obtain();
							message.what = READ_FAILED;
							handler.sendMessage(message);

						} else if (sizee == -2) {
							 Message message = Message.obtain(); 
							 message.what= READ_BLOCKED;
							 handler.sendMessage(message);

						} else if (sizee == -3) {
							Message message = Message.obtain();
							message.what = READ_UNEXCEPTED;
							handler.sendMessage(message);
						}

					} catch (Exception e) {
						Log.e(TAG, "serverRunnable 44444 e =" + e);
					}

					// Log.d(TAG, "serverRunnable mServerList.size() =" + mServerList.size());
					if (mServerList.size() > 0) {

						for (int i = 0; i < mServerList.size(); i++) {
							serverString = mServerList.get(i);
							/*if (serverString != null) {
								String server2 = StrToBinstr(serverString);
								Log.d(TAG, "serverRunnable read server2= " + server2);

								Log.d(TAG, "serverRunnable 11 serverString ="
										+ serverString +"{{{}=" + serverString.length());
							}*/

							if (serverString != null) {
								try {
									serverString = new String(serverString.getBytes("UTF-8"));
									
									int serverWrite = serverApi
											.KSY_Device_Session_Write(
													serverSid, serverString,
													serverString.getBytes().length, 0);
//									Log.e(TAG, "serverRunnable 22 serverWrite =" + serverWrite + ">>serverString=" + serverString + "<>>" + serverString.getBytes().length);
									if (serverWrite == -1) {
										Message message = Message.obtain();
										message.what = WRITE_FAILED;
										handler.sendMessage(message);

									} else if (serverWrite == -2) {
										Message message = Message.obtain();
										message.what = WRITE_BLOCKED;
										handler.sendMessage(message);

									} else if (serverWrite == -3) {
										Message message = Message.obtain();
										message.what = WRITE_UNEXCEPTED;
										handler.sendMessage(message);
									}

								} catch (Exception e) {
									Log.e(TAG, "serverRunnable 555555 e =" + e);
								}
							} else {
								Log.e(TAG,
										"serverRunnable  serverString = null");
							}

							serverString = null;
						}
					}

					mServerList.clear();

				} else {
					Log.d(TAG, "serverRunnable  server = " + server);
				}
			}
		}
	};

	/**
	 * message handle
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CLIENT_SEND:
				// 实体数据添加
				KsyMsgEntity entity = new KsyMsgEntity();
				entity.setName("device"); // client
				entity.setDate(getDate());
				entity.setMessage(clientData);
				entity.setMsgType(false);

				mDataArrays.add(entity);

				mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
				mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项

				break;

			case SERVER_SEND:
				// 实体数据添加
				KsyMsgEntity entityServer = new KsyMsgEntity();
				entityServer.setName("client"); // server
				entityServer.setDate(getDate());
				entityServer.setMessage(serverData);
				entityServer.setMsgType(false);

				mDataArrays.add(entityServer);

				mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
				mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项

				break;

			case CONNECT_SUCCEED:
				Toast.makeText(MainActivity.this, R.string.connect_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_FAILED:
				Toast.makeText(MainActivity.this, R.string.connect_failed,
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_TIMEOUT:
				Toast.makeText(MainActivity.this, R.string.connect_timeout,
						Toast.LENGTH_SHORT).show();
				break;
				
			case SESSION_CHECK:
				Toast.makeText(MainActivity.this,
						R.string.session_check_failed, Toast.LENGTH_SHORT)
						.show();
				break;

			case READ_FAILED:
				Toast.makeText(MainActivity.this, R.string.read_failed,
						Toast.LENGTH_SHORT).show();
				break;

			case READ_BLOCKED:
				Toast.makeText(MainActivity.this, R.string.read_blocked,
						Toast.LENGTH_SHORT).show();
				break;

			case READ_UNEXCEPTED:
				Toast.makeText(MainActivity.this, R.string.read_unexpected,
						Toast.LENGTH_SHORT).show();
				break;

			case WRITE_FAILED:
				Toast.makeText(MainActivity.this, R.string.write_failed,
						Toast.LENGTH_SHORT).show();
				break;

			case WRITE_BLOCKED:
				Toast.makeText(MainActivity.this, R.string.write_blocked,
						Toast.LENGTH_SHORT).show();
				break;

			case WRITE_UNEXCEPTED:
				Toast.makeText(MainActivity.this, R.string.write_unexpected,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	/**
	 * init view
	 */
	private void initView() {

		mListView = (ListView) findViewById(R.id.listview);

		mButtonSend = (Button) findViewById(R.id.btn_send);
		mButtonSend.setOnClickListener(this);
		mButtonServer = (Button) findViewById(R.id.btn_server);
		mButtonServer.setOnClickListener(this);
		mButtonClient = (Button) findViewById(R.id.btn_client);
		mButtonClient.setOnClickListener(this);

		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		mAdapter = new KsyMsgAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);

		// 下拉框
		serverSpinner = (Spinner) findViewById(R.id.spinner_server);
		clientSpinner = (Spinner) findViewById(R.id.spinner_client);

		// 将可选内容与ArrayAdapter连接起来
		serverAdapter = ArrayAdapter.createFromResource(this,
				R.array.server_list, android.R.layout.simple_spinner_item);
		clientAdapter = ArrayAdapter.createFromResource(this,
				R.array.client_list, android.R.layout.simple_spinner_item);

		// 设置下拉列表的风格
		serverAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		clientAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// 将adapter添加到spinner中
		serverSpinner.setAdapter(serverAdapter);
		clientSpinner.setAdapter(clientAdapter);

		serverSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View v,
					int position, long arg3) {
				Log.d(TAG, "serverAdapter.getItem(position)=" + position
						+ ">>>" + serverAdapter.getItem(position));
				serverUID = (String) serverAdapter.getItem(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		clientSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View v,
					int position, long arg3) {
				Log.d(TAG, "clientAdapter.getItem(position)=" + position
						+ ">>>" + clientAdapter.getItem(position));
				clientUID = (String) clientAdapter.getItem(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		serverSpinner.setSelection(0, true);
		clientSpinner.setSelection(0, true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {

			mEditTextContent.setText("");

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * click
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:// 发送按钮
			send();

			break;

		case R.id.btn_server:// 服务器端按钮
			serverListener();

			break;

		case R.id.btn_client: // 客户端按纽
			clientListener();

			break;
		}
	}

	private void serverListener() {

		mHandler.post(serverRunnable);

		server = 1;

		mButtonClient.setClickable(false);
	}

	private void clientListener() {

		mHandler.post(clientRunnable);

		client = 1;

		mButtonServer.setClickable(false);
	}

	/**
	 * send button
	 */
	private void send() {

//		String contString = mEditTextContent.getText().toString();
		contentUtf = mEditTextContent.getText().toString();
//		Log.d(TAG, "send() contString ==" + contString);

		/*try {
			contentUtf = toUTF_8(contString);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
*/
		Log.d(TAG, "send() contentUtf.length() ==" + contentUtf.length());
		if (contentUtf.length() > 0) {
			Log.d(TAG, "send() server =" + server + ">>client =" + client);
			if (server == 1) {// 服务端

				mServerList.add(contentUtf); // 添加到服务端队列

				// 实体数据添加
				KsyMsgEntity entityServer = new KsyMsgEntity();
				entityServer.setName("device");
				entityServer.setDate(getDate());
				entityServer.setMessage(contentUtf);
				entityServer.setMsgType(false);

				mDataArrays.add(entityServer);

			} else if (client == 1) { // 客户端

				mClientList.add(contentUtf); // 添加到客户端队列

				// 实体数据添加
				KsyMsgEntity entity = new KsyMsgEntity();
				entity.setName("client");
				entity.setDate(getDate());
				entity.setMessage(contentUtf);
				entity.setMsgType(false);

				mDataArrays.add(entity);
			}

		} else {
			// TODO
		}

		mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
		mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项

		mEditTextContent.setText("");// 清空编辑框数据
		contentUtf = null;
	}

	/**
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String toUTF_8(String str) throws UnsupportedEncodingException {
		return this.changeCharset(str, "UTF_8");
	}

	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换编码的字符串
	 * @param newCharset
	 *            目标编码
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String changeCharset(String str, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用默认字符编码解码字符串。
			byte[] bs = str.getBytes();
			// 用新的字符编码生成字符串
			return new String(bs, newCharset);
		}
		return null;
	}

	/**
	 * 发送消息时，获取当前事件
	 * 
	 * @return 当前时间
	 */
	private String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return format.format(new Date());
	}

	@Override
	protected void onResume() {
		mClientRunning = true;
		mServerRunning = true;

		super.onResume();

		Log.d(TAG, "onResume()............");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		Log.d(TAG, "onStart()............");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		Log.d(TAG, "onPause()............");
	}

	@Override
	protected void onStop() {
		mClientRunning = false;
		mServerRunning = false;

		super.onStop();
		Log.d(TAG, "onStop()............");
	}

	@Override
	protected void onDestroy() {
		mClientRunning = false;
		mServerRunning = false;

		super.onDestroy();

		if (server == 1) {
			handler.removeCallbacks(serverRunnable);
			handler.removeMessages(CONNECT_SUCCEED);
			handler.removeMessages(SERVER_SEND);

		} else if (client == 1) {
			handler.removeCallbacks(clientRunnable);
			handler.removeMessages(CLIENT_SEND);
			handler.removeMessages(SESSION_CHECK);
		}

		handler.removeMessages(INIT_ERROR);
		handler.removeMessages(READ_BLOCKED);
		handler.removeMessages(READ_FAILED);
		handler.removeMessages(READ_UNEXCEPTED);
		handler.removeMessages(WRITE_BLOCKED);
		handler.removeMessages(WRITE_FAILED);
		handler.removeMessages(WRITE_UNEXCEPTED);

		Log.d(TAG, "onDestroy()............");

	}

	/**
	 * back
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (server == 1) {// 服务端

				try {
					serverApi.KSY_Device_Session_Close(serverSid);
				} catch (Exception e) {
					Log.e(TAG, "onKeyDown  server e=" + e);
				}

			} else if (client == 1) { // 客户端

				try {
					clientApi.KSY_Client_Session_Close(clientsid);
				} catch (Exception e) {
					Log.e(TAG, "onKeyDown  client e=" + e);
				}
			}

			finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// TODO
	private void isDisconnect() {

	}
	
	//将字符串转换成二进制字符串，以空格相隔
    private String StrToBinstr(String str) {
        char[] strChar=str.toCharArray();
        String result="";
        for(int i=0;i<strChar.length;i++){
            result +=Integer.toBinaryString(strChar[i])+ " ";
        }
        return result;
    }
    
}



