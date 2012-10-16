package com.profiler.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import com.profiler.Agent;

/**
 * Span represent RPC
 * 
 * @author netspider
 * 
 */
public class Span {

	private final TraceID traceID;
	private final long createTime;

	private String serviceName;
	private String name;
	private String endPoint;
	private boolean isTerminal = false;

	private final List<HippoAnnotation> annotations = new ArrayList<HippoAnnotation>(5);
	private final Set<String> annotationKeys = new HashSet<String>(5);
	
	private long rpcStartTime;
	private long rpcEndTime;
	
	/**
	 * Cancel timer logic.
	 * TODO: refactor this.
	 */
	private TimerTask timerTask;

	public void setTimerTask(TimerTask task) {
		this.timerTask = task;
	}

	public boolean cancelTimer() {
		return timerTask.cancel();
	}

	public Span(TraceID traceId, String name, String endPoint) {
		this.traceID = traceId;
		this.name = name;
		this.endPoint = endPoint;
		this.createTime = System.currentTimeMillis();
	}

	public boolean addAnnotation(HippoAnnotation annotation) {
		annotationKeys.add(annotation.getKey());
		if (annotation.getKey().equals(Annotation.ClientSend.getCode()) || annotation.getKey().equals(Annotation.ServerRecv.getCode())) {
			rpcStartTime = annotation.getTimestamp();
		}
		if (annotation.getKey().equals(Annotation.ClientRecv.getCode()) || annotation.getKey().equals(Annotation.ServerSend.getCode())) {
			rpcEndTime = annotation.getTimestamp();
		}
		return annotations.add(annotation);
	}

	public int getAnnotationSize() {
		return annotations.size();
	}

	/**
	 * this method only works for Trace.mutate()
	 * 
	 * @param value
	 * @return
	 */
	public boolean isExistsAnnotationKey(String key) {
		return annotationKeys.contains(key);
	}

	public String getEndPoint() {
		return this.endPoint;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
	
	public boolean isTerminal() {
		return isTerminal;
	}

	public void setTerminal(boolean isTerminal) {
		this.isTerminal = isTerminal;
	}
	
	public long getRpcStartTime() {
		return rpcStartTime;
	}

	public long getRpcEndTime() {
		return rpcEndTime;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{");
		sb.append("\n\t TraceID = ").append(traceID);
		sb.append(",\n\t CreateTime = ").append(createTime);
		sb.append(",\n\t Name = ").append(name);
		sb.append(",\n\t ServiceName = ").append(serviceName);
		sb.append(",\n\t EndPoint = ").append(endPoint);

		sb.append(",\n\t Annotations = {");
		for (HippoAnnotation a : annotations) {
			sb.append("\n\t\t").append(a);
		}
		sb.append("\n\t}");

		sb.append("}");

		return sb.toString();
	}
	
	public com.profiler.common.dto.thrift.Span toThrift() {
		com.profiler.common.dto.thrift.Span span = new com.profiler.common.dto.thrift.Span();

		span.setAgentID(Agent.getInstance().getAgentId());
		span.setTimestamp(createTime);
		span.setMostTraceID(traceID.getId().getMostSignificantBits());
		span.setLeastTraceID(traceID.getId().getLeastSignificantBits());
		span.setName(name);
		span.setServiceName(serviceName);
		span.setSpanID(traceID.getSpanId());
		span.setParentSpanId(traceID.getParentSpanId());
		span.setEndPoint(endPoint);
		span.setTerminal(isTerminal);
		
		// TODO: set duration.
		
		List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(annotations.size());
		for (HippoAnnotation a : annotations) {
			annotationList.add(a.toThrift());
		}
		span.setAnnotations(annotationList);

		span.setFlag(traceID.getFlags());

		return span;
	}
}
