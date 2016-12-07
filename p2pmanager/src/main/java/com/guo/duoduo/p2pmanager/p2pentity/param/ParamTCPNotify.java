package com.guo.duoduo.p2pmanager.p2pentity.param;


import com.guo.duoduo.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by zeus on 2016/9/22.
 */
public class ParamTCPNotify
{
    public P2PNeighbor Neighbor;
    public Object Obj;

    public ParamTCPNotify(P2PNeighbor dest, Object obj)
    {
        Neighbor = dest;
        Obj = obj;
    }
}
