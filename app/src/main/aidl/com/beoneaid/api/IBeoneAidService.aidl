package com.beoneaid.api;
import com.beoneaid.api.IBeoneAidServiceCallback;

interface IBeoneAidService {
    void startSpeaking(String s);
    void registerCallback(IBeoneAidServiceCallback cb);
    void unregisterCallback(IBeoneAidServiceCallback cb);
}