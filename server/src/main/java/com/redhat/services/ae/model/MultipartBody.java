package com.redhat.services.ae.model;

import java.io.File;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class MultipartBody{
  @javax.ws.rs.FormParam("file")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public InputStream file;

  @javax.ws.rs.FormParam("fileName")
  @PartType(MediaType.TEXT_PLAIN)
  public String fileName;
}
