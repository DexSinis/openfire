package com.d3.push;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javapns.*;
import javapns.back.PushNotificationManager;
import javapns.data.Device;
import javapns.data.PayLoad;
import javapns.back.SSLConnectionHelper;
//import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServerBasicImpl;
//import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;


import org.apache.commons.lang.StringUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;




/**
 * <b>function:</b> send offline msg plugin
 * @author MZH
 */
public class PushInterceptor implements PacketInterceptor{
    //Hook for intercpetorn
    private InterceptorManager interceptorManager;
    private UserManager userManager;
    private PresenceManager presenceManager;
    
    public PushInterceptor(){
        interceptorManager = InterceptorManager.getInstance();
        interceptorManager.addInterceptor(this);
        
        
        XMPPServer server = XMPPServer.getInstance();
        userManager = server.getUserManager();
        presenceManager = server.getPresenceManager();
        System.out.println("-----------PushInterceptor");
    }
    
    /**
     * intercept message
     */
    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
        if (processed || !(packet instanceof Message) || !incoming || Message.Type.chat != ((Message) packet).getType())
            return;
        this.doAction(packet, incoming, processed, session);
        
        
    }
    
    /**
     * <b>send offline msg from this function </b>
     */
    private void doAction(Packet packet, boolean incoming, boolean processed,
                          Session session) {
    	System.out.println("-----------doAction");
        Message message = (Message) packet;
        JID recipient = message.getTo();
        // get message
        try {
            // if (recipient.getNode() == null
            // || !UserManager.getInstance().isRegisteredUser(recipient.getNode())) {
            // // Sender is requesting presence information of an anonymous
            // //throw new UserNotFoundException("Username is null");
            // }
            
            
            Presence status = presenceManager.getPresence(userManager.getUser(recipient.getNode()));
            if (status == null) {  //offline
                String deviceToken = getDeviceToken(recipient.getNode());
                if(isApple(deviceToken))
                    pns(deviceToken, message.getBody());
            }// end if
            
            
        } catch (UserNotFoundException e) {
            System.out.println("user not found");
            // e.printStackTrace();
        }
    }
    
    /**
     * 判断是否苹果
     * @param deviceToken
     * @return
     */
    private boolean isApple(String deviceToken){
        if(deviceToken!=null&&deviceToken.length()>0){
            return true;
        }
        return false;
    }
    
    public String getDeviceToken(String userId) {
        String deviceToken = "";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement("SELECT devicetoken FROM ofAPNS where JID = ?");
            pstmt.setString(1, "dexsinis");
          
            rs = pstmt.executeQuery();
            if (rs.next()) {
                deviceToken = rs.getString(1);
            }
            System.out.println(deviceToken+"-----------deviceToken");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return deviceToken;
    }
    
    public void pns2(String token, String msg) {
//        String sound = "default";// 铃音
//        String certificatePath =  JiveGlobals.getProperty("plugin.push.apnsPath");
//        System.out.println(certificatePath+"-----------certificatePath");
//        String certificatePassword = JiveGlobals.getProperty("plugin.push.apnsKey"); // 此处注意导出的证书密码不能为空因为空密码会报错
//        System.out.println(certificatePassword+"-----------certificatePassword");
//        boolean isProduct = JiveGlobals.getBooleanProperty("plugin.push.isProduct");
//        System.out.println(isProduct+"-----------isProduct");
//        try {
//            PushNotificationPayload payLoad = new PushNotificationPayload();
//            payLoad.addAlert(msg); // 消息内容
//            payLoad.addBadge(1); // iphone应用图标上小红圈上的数值
//            if (!StringUtils.isBlank(sound)) {
//                payLoad.addSound(sound);// 铃音
//            }
//            PushNotificationManager pushManager = new PushNotificationManager();
//       
//            // true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
//            pushManager.initializeConnection(new AppleNotificationServerBasicImpl(
//                                                                                  certificatePath, certificatePassword, isProduct));
//            
////            pushManager.initializeConnection(host, port, certificatePath, certificatePassword, SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
//            // 发送push消息
//            Device device = new BasicDevice();
//            device.setToken(token);
//            PushedNotification notification = pushManager.sendNotification(
//                                                                           device, payLoad, true);
//            List<PushedNotification> notifications = new ArrayList<PushedNotification>();
//            notifications.add(notification);  
//            List<PushedNotification> failedNotifications = PushedNotification.findFailedNotifications(notifications);  
//            List<PushedNotification> successfulNotifications = PushedNotification.findSuccessfulNotifications(notifications);
//            System.out.println(isProduct+"-----------isProduct");
//            int failed = failedNotifications.size();  
//            int successful = successfulNotifications.size();  
//            if (successful > 0 && failed == 0) {  
//            	 System.out.println("-----All notifications pushed 成功 (" + successfulNotifications.size() + "):");  
//            } else if (successful == 0 && failed > 0) {  
//            	 System.out.println("-----All notifications 失败 (" + failedNotifications.size() + "):");  
//            } else if (successful == 0 && failed == 0) {  
//            System.out.println("No notifications could be sent, probably because of a critical error");  
//            } else {  
//            	 System.out.println("------Some notifications 失败 (" + failedNotifications.size() + "):");  
//            	 System.out.println("------Others 成功 (" + successfulNotifications.size() + "):");  
//            }  
//            
//            pushManager.stopConnection();
//        } catch (Exception e) {
//        	 System.out.println(e);
//            e.printStackTrace();
//        }
    }
    
    
    
    @SuppressWarnings("deprecation")
	public void pns(String token, String msg) { 
    
    try
    {
    	 System.out.println("pns-----------------");
        //从客户端获取的deviceToken，在此为了测试简单，写固定的一个测试设备标识。
       String deviceToken = token;
        System.out.println("Push Start deviceToken:---------" + deviceToken);
        //定义消息模式
        PayLoad payLoad = new PayLoad();
        payLoad.addAlert(msg);
        payLoad.addBadge(1);//消息推送标记数，小红圈中显示的数字。
        payLoad.addSound("default");
        //注册deviceToken
        PushNotificationManager pushManager = PushNotificationManager
				.getInstance();
        pushManager.addDevice("iPhone", deviceToken);
        //连接APNS
        String host = "gateway.sandbox.push.apple.com";
        //String host = "gateway.push.apple.com";
        int port = 2195;
      String certificatePath =  JiveGlobals.getProperty("plugin.push.apnsPath");
      System.out.println(certificatePath+"-----------certificatePath");
      String certificatePassword = JiveGlobals.getProperty("plugin.push.apnsKey"); // 此处注意导出的证书密码不能为空因为空密码会报错
      System.out.println(certificatePassword+"-----------certificatePassword");
      boolean isProduct = JiveGlobals.getBooleanProperty("plugin.push.isProduct");
      System.out.println(isProduct+"-----------isProduct");
        pushManager.initializeConnection(host, port, certificatePath, certificatePassword, SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
        //发送推送
        Device client = pushManager.getDevice("iPhone");
        System.out.println("推送消息: " + client.getToken()+"\n"+payLoad.toString() +" ");
//        pushManager.sendNotification(client, payLoad);
        //停止连接APNS
        pushManager.stopConnection();
        //删除deviceToken
        pushManager.removeDevice("iPhone");
        System.out.println("Push End");
    }
    catch (Exception ex)
    {
        ex.printStackTrace();
    }
   }
}

