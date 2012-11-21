package org.jboss.bpm.console.server.util; 

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RsComment
{
  public abstract String title();

  public abstract String description();

  public abstract ProjectName[] project();

  public abstract String example();
}