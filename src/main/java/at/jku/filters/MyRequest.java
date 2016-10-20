package at.jku.filters;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Forwarding;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Pipe;

import org.apache.catalina.connector.Request;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class MyRequest {
	private static final Log log = LogFactory.getLog(MyRequest.class);
	private Request req;

	public MyRequest(Request req) {
		this.req = req;
	}

	private Object transcode(final Object o) {
		final String enc = (req.getCharacterEncoding() != null) ? req
				.getCharacterEncoding() : "ISO-8859-1";
		if (o instanceof String && o != null) {
			try {
				return (Object) new String(((String) o).getBytes("ISO-8859-1"),
						enc);
			} catch (UnsupportedEncodingException e) {
				log.info("Unsupported encoding given, returning attribute unmodified");
				return o;
			}
		}
		return o;
	}

	public static Request newInstance(Request req) {
		MethodDelegation delegate = MethodDelegation.to(new MyRequest(req)).appendParameterBinder(Pipe.Binder.install(Forwarder.class));
		Request proxy = null;
		try {
			proxy = new ByteBuddy()
				.subclass(Request.class)
				.implement(HttpServletRequest.class)
				// ...forward other methods to original object
				.method(isDeclaredBy(Request.class)).intercept(Forwarding.to(req))
				// Intercept target methods...
				.method(named("getAttributeNames").or(
						named("getAttribute")).or(
						named("getRemoteUser")).or(
						named("getHeader")))
						.intercept(delegate)
				.make()
				// INJECTION - Required for messing with tomcat classes beyond web-app classloader.
				.load(MyRequest.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
				.getLoaded()
				.newInstance();
		} catch (Exception e) {
			log.error("Error constructing proxy - passing original request", e);
			return req;
		}
    	return proxy;
    }

	// ------------------------------------------------------------- Proxied methods
	public Enumeration<String> getAttributeNames(@Pipe Forwarder<Enumeration<String>, Request> pipe) {
		log.debug("modified getAttributeNames() called");
		Set<String> names = new HashSet<String>(Collections.list(pipe.to(req)));
		names.addAll(req.getCoyoteRequest().getAttributes().keySet());
		return Collections.enumeration(names);
	}

	public Object getAttribute(@Pipe Forwarder<Object, Request> pipe, String name) {
		log.debug("modified getAttribute() called"); 
		final Object o = pipe.to(req);
		return transcode(o);
	}

	public String getHeader(@Pipe Forwarder<String, Request> pipe, String name) {
		log.debug("modified getHeader() called");
		return (String) transcode(pipe.to(req));
	}
	
	public String getRemoteUser(@Pipe Forwarder<String, Request> pipe) {
		log.debug("modified getRemoteUser() called");
		return (String) transcode(pipe.to(req));
	}
}
