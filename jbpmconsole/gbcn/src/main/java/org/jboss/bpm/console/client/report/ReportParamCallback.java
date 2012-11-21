package org.jboss.bpm.console.client.report;

import java.util.Map;

public abstract interface ReportParamCallback
{
  public abstract void onSumbit(Map<String, String> paramMap);
}