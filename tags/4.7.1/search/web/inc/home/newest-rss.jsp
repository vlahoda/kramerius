<%@ page contentType="text/xml" pageEncoding="UTF-8" %><rss version="2.0">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>

  <channel> 

<view:object name="rssHome"
 clz="cz.incad.Kramerius.views.inc.home.RSSHomeViewObject"></view:object> 

  <title><view:msg>application.title</view:msg></title> 
  <description><view:msg>home.tab.newest</view:msg></description> 
  <link>   <c:out value="${rssHome.channelURL}" escapeXml="true" />   </link> 
<c:url var="url" value="${rssHome.configuration.solrHost}/select" >
    <c:param name="q" value="level:0" />
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows" value="18" scope="request" />
        </c:otherwise>
    </c:choose>
    <view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
    <c:if test="${cols.current != null}">
        <c:param name="fq" value="collection:\"${cols.current.pid}\"" />
    </c:if>
    <c:param name="rows" value="${rows}" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
        <c:set var="filters" scope="request"><c:out value="${filters}" />&fq=<c:out value="${fqs}" /></c:set>
    </c:forEach>
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="level asc, created_date desc" />
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <x:parse var="doc" xml="${xml}"  />
        
        <x:forEach varStatus="status" select="$doc/response/result/doc">
            <c:set var="pid"><x:out select="./str[@name='PID']"/></c:set>
            <c:set var="t"><x:out select="./str[@name='root_title']"/></c:set>
            <c:set var="title"><x:out select="./str[@name='dc.title']"/></c:set>
            <c:set var="fmodel"><x:out select="./str[@name='fedora.model']"/></c:set>
            <item>
                <title>${title}</title>    
                <description>PID: ${pid} Model: <view:msg>${fmodel}</view:msg>     
                </description>
                <link>${rssHome.applicationURL}/handle/${pid}</link>
                <guid>${rssHome.applicationURL}/handle/${pid}</guid>
            </item>
        </x:forEach>
        
    </c:otherwise>
</c:choose>
<c:if test="${param.debug}" >
    <c:out value="${url}" /><br/>
    <c:out value="${param.parentPid}" />
</c:if>
        
  </channel>
  </rss>

