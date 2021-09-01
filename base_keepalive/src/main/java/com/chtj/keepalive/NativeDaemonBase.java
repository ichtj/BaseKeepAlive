package com.chtj.keepalive;

import com.chtj.keepalive.impl.IDaemonStrategy;

/**
 * native base class
 * 
 * @author Mars
 *
 */
public class NativeDaemonBase {
    /**
     * native call back
     */
	protected void onDaemonDead(){
		IDaemonStrategy.Fetcher.fetchStrategy().onDaemonDead();
    }
    
}
