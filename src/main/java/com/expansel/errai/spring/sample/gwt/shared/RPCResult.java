package com.expansel.errai.spring.sample.gwt.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Bus marshalling at work.
 *
 *
 * @author Zach Visagie
 */
@Portable
public class RPCResult {
    private String result;

    public RPCResult() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
