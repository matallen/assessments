package com.redhat.services.ae.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FluentCalendar {
  private Calendar c=Calendar.getInstance();
  public static FluentCalendar get(){
    return new FluentCalendar();
  }
  public static FluentCalendar get(Date time){
    return new FluentCalendar(time);
  }
  public FluentCalendar(){}
  public FluentCalendar(Date time){
    c.setTime(time);
  }
  
  public FluentCalendar add(int field, int amount){
    c.add(field, amount);
    return this;
  }
  public FluentCalendar set(int field, int amount){
    c.set(field, amount);
    return this;
  }
  
  /** yyyy-MM-dd HH:mm:ss Z */
  public String getString(String format){
    return new SimpleDateFormat(format).format(c.getTime());
  }
  
  public Calendar build(){
    return c;
  }
}
