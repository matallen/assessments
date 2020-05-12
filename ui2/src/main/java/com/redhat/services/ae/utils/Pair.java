package com.redhat.services.ae.utils;

public class Pair<L,R>{
  private L left;
  private R right;
  
  public Pair(L left, R right){
    this.left=left;
    this.right=right;
  }
  public L getFirst(){
    return left;
  }
  public void setFirst(L left){
    this.left=left;
  }
  public R getSecond(){
    return right;
  }
  public void setSecond(R right){
    this.right=right;
  }
  public String toString(){
    return Pair.class.getSimpleName()+"(first="+left+", second="+right+")";
  }
}