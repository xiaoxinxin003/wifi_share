package com.guo.duoduo.p2pmanager.p2pinterface;


import com.guo.duoduo.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by zeus on 2016/9/19.
 * 局域网好友上线和掉线
 */
public interface Melon_Callback
{
    /**
     * 局域网发现好友
     * @param melon
     */
    public void Melon_Found(P2PNeighbor melon);

    /**
     * 局域网好友离开
     * @param melon
     */
    public void Melon_Removed(P2PNeighbor melon);
}
