package com.redhat.services.ae;

import java.util.Random;

public class Utils{

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  public static String toIndentedString(java.lang.Object o) {
  	return toIndentedString(o, 4);
  }
	public static String toIndentedString(java.lang.Object o, int spaces) {
    if (o == null) return "null";
    return o.toString().replace("\n", "\n"+String.format("%"+spaces+"s", " "));
  }
	
	public static synchronized String generateId(){
    return new Random().ints(97 /*letter 'a'*/, 122 /*letter 'z'*/ + 1)
      .limit(6)
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString().toUpperCase();
	}
	
}
