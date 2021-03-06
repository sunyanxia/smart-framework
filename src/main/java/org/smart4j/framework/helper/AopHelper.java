package org.smart4j.framework.helper;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Aspect;
import org.smart4j.framework.proxy.AspectProxy;
import org.smart4j.framework.proxy.Proxy;
import org.smart4j.framework.proxy.ProxyManager;

/**
 * 
 * @ClassName AopHelper  
 * @Description  
 * @author chengzhb 
 * @date 2018年8月14日  
 *   
 */
public final class AopHelper {
	private static final Logger LOGGER=LoggerFactory.getLogger(AopHelper.class);
	static {
		try {
			Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
			Map<Class<?>, List<Proxy>> targetMap = createTargetMap(proxyMap);
			for(Map.Entry<Class<?>, List<Proxy>> targetEntry:targetMap.entrySet()) {
				Class<?> targetClass = targetEntry.getKey();
				List<Proxy> proxyList = targetEntry.getValue();
				Object proxy=ProxyManager.createProxy(targetClass, proxyList);
				BeanHelper.setBean(targetClass, proxy);
			}
		} catch (Exception e) {
			LOGGER.error("aop failure",e);
		}
	}
private static Set<Class<?>> createTargetClsssSet(Aspect aspect)throws Exception{
	Set<Class<?>> targetClassSet=new HashSet<Class<?>>();
	Class<? extends Annotation> annotation=aspect.value();
	if(annotation!=null&&!annotation.equals(Aspect.class)) {
		targetClassSet.addAll(ClassHelper.getClassSetByAnnotation(annotation));
	}
	return targetClassSet;
}
private static Map<Class<?>,Set<Class<?>>> createProxyMap() throws Exception{
	Map<Class<?>,Set<Class<?>>> proxyMap=new HashMap<Class<?>,Set<Class<?>>>();
	Set<Class<?>> proxyClassSet = ClassHelper.getClassSetBySuper(AspectProxy.class);
	for(Class<?> proxyClass:proxyClassSet) {
		if(proxyClass.isAnnotationPresent(Aspect.class)) {
			Aspect aspect=proxyClass.getAnnotation(Aspect.class);
			Set<Class<?>> targetClassSet=createTargetClsssSet(aspect);
			proxyMap.put(proxyClass, targetClassSet);
		}
	}
	return proxyMap;
}
private static Map<Class<?>,List<Proxy>> createTargetMap(Map<Class<?>,Set<Class<?>>> proxyMap)throws Exception{
	Map<Class<?>,List<Proxy>> targetMap=new HashMap<Class<?>,List<Proxy>>();
	for(Map.Entry<Class<?>,Set<Class<?>>> proxyEntry:proxyMap.entrySet()) {
		Class<?> proxyClass=proxyEntry.getKey();
		Set<Class<?>> targetClassSet=proxyEntry.getValue();
		for(Class<?> targetClass:targetClassSet) {
			Proxy proxy=(Proxy) proxyClass.newInstance();
			if(targetMap.containsKey(targetClass)) {
				targetMap.get(targetClass).add(proxy);
			}else {
				List<Proxy> proxyList=new ArrayList<Proxy>();
				proxyList.add(proxy);
				targetMap.put(targetClass, proxyList);
			}
		}
	}
	return targetMap;
}
}
