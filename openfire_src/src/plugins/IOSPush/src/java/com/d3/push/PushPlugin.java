package com.d3.push;


import java.io.File;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.interceptor.InterceptorManager;

import com.wecapslabs.openfire.plugin.apns.ApnsIQHandler;


public class PushPlugin implements Plugin {
    
    private PushInterceptor pushInterceptor = null;
    
    
    @Override
    public void destroyPlugin() {
        if(pushInterceptor != null){
            InterceptorManager.getInstance().removeInterceptor(pushInterceptor);
        }
    }
    
    
    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        
    	System.out.println("-----------PushIQHandler--------PushIQHandler");

    	  IQHandler myHandler = new PushIQHandler();
          IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
          iqRouter.addHandler(myHandler);
    	
        pushInterceptor = new PushInterceptor();
        System.out.println("-----------initializePlugin--------pushInterceptor");
        InterceptorManager.getInstance().addInterceptor(pushInterceptor);
    }
    
    
}

