<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>
    
<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<table width="100%">
    <c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />
    <c:set var="others" value="" />
    <c:set var="urlStr" >
        <c:choose>
            <c:when test="${param.model == 'info:fedora/model:page'}">
                <c:out value="${fedoraHost}" />/get/<c:out value="${param.parentPid}" />/BIBLIO_MODS
                <c:set var="display" value="none"/>
                <c:choose>
                    <c:when test="${param.parentModel == 'info:fedora/model:monograph'}">
                        <c:set var="modelForInfo" value="monograph"/>
                    </c:when>
                    <c:when test="${param.parentModel == 'info:fedora/model:monographunit'}">
                        <c:set var="modelForInfo" value="monographunit"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="modelForInfo" value="monograph"/>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:when test="${param.model == 'info:fedora/model:monograph'}">
                    <c:out value="${fedoraHost}" />/get/<c:out value="${param.pid}" />/BIBLIO_MODS
                    <c:set var="display" value="block"/>
                    <c:set var="modelForInfo" value="monograph"/>
            </c:when>
            <c:when test="${param.model == 'info:fedora/model:monographunit'}">
                    <c:out value="${fedoraHost}" />/get/<c:out value="${param.pid}" />/BIBLIO_MODS
                    <c:set var="display" value="block"/>
                    <c:set var="modelForInfo" value="monographunit"/>
            </c:when>
            <c:when test="${param.model == 'info:fedora/model:periodical'}">
                    <c:out value="${fedoraHost}" />/get/<c:out value="${param.pid}" />/BIBLIO_MODS
                    <c:set var="display" value="block"/>
                    <c:set var="modelForInfo" value="periodical"/>
            </c:when>
            <c:when test="${param.model == 'info:fedora/model:periodicalvolume'}">
                    <c:out value="${fedoraHost}" />/get/<c:out value="${param.pid}" />/BIBLIO_MODS
                    <c:set var="display" value="none"/>
                    <c:set var="modelForInfo" value="periodicalvolume"/>
            </c:when>
            <c:when test="${param.model == 'info:fedora/model:periodicalitem'}">
                    <c:out value="${fedoraHost}" />/get/<c:out value="${param.pid}" />/BIBLIO_MODS
                    <c:set var="display" value="none"/>
                    <c:set var="modelForInfo" value="periodicalitem"/>
            </c:when>
            <c:otherwise>
                    not implemented <c:out value="${param.model}" />
            </c:otherwise>
        </c:choose>
    </c:set>
    <c:url var="urlGet" value="${urlStr}" >
    </c:url>
    <c:catch var="exceptions"> 
        <c:import url="${urlGet}" var="xml2" charEncoding="UTF-8"  />
        <c:import url="details/${modelForInfo}.jsp?display=${display}&language=${param.language}${others}" var="xslt" charEncoding="UTF-8"  />
    </c:catch>
    <c:choose>
        <c:when test="${exceptions != null}" >
            <c:out value="${exceptions}" /><br/><br/>
        </c:when>
        <c:otherwise>
                <x:transform doc="${xml2}"  xslt="${xslt}"  /><br/><br/>
        </c:otherwise>
    </c:choose>
    <%-- zobrazime rels ext --%>    
    <c:choose>
        <c:when test="${param.model == 'info:fedora/model:page'}">
            <%@ include file="details/page.jsp"  %>
        </c:when>
        <c:when test="${param.model == 'info:fedora/model:monograph'}">
            <tr><td colspan="2">
            <div id="pages"></div>
            </td></tr>
            <tr><td colspan="2">
            <div id="units"></div>
            </td></tr>
            <tr><td colspan="2">
            <div id="internalParts"></div>
            </td></tr>
            <script type="text/javascript"> 
		$(document).ready(function(){
			getPagesList('<c:out value="${param.pid}" />');
                        getUnitsList('<c:out value="${param.pid}" />');
                        getInternalPartList('<c:out value="${param.pid}" />');
                        
		});
	</script>
        </c:when>
        <c:when test="${param.model == 'info:fedora/model:monographunit'}">
            <tr><td colspan="2">
            <div id="pages"></div>
            </td></tr>
            <tr><td colspan="2">
            <div id="internalParts"></div>
            </td></tr>
            <script type="text/javascript"> 
		$(document).ready(function(){
			getPagesList('<c:out value="${param.pid}" />');
                        getInternalPartList('<c:out value="${param.pid}" />');
		});
	</script>
        </c:when>
        <c:when test="${param.model == 'info:fedora/model:periodical'}">
            <tr><td colspan="2">
            <div id="volumes"></div>
            </td></tr>
            <script type="text/javascript"> 
		$(document).ready(function(){
			getVolumeList('<c:out value="${param.pid}" />');
		});
	</script>
        </c:when>
        <c:when test="${param.model == 'info:fedora/model:periodicalvolume'}">
            <tr><td colspan="2">
            <div id="issues"></div>
            </td></tr>
            <tr><td colspan="2">
            <div id="pages"></div>
            </td></tr>
            <script type="text/javascript"> 
		$(document).ready(function(){
			getIssuesList('<c:out value="${param.pid}" />');
			getPagesList('<c:out value="${param.pid}" />');
		});
	</script>
        </c:when>
        <c:when test="${param.model == 'info:fedora/model:periodicalitem'}">
            <tr><td colspan="2">
            <div id="pages"></div>
            </td></tr>
            <script type="text/javascript"> 
		$(document).ready(function(){
			getPagesList('<c:out value="${param.pid}" />');
		});
	</script>
        </c:when>
        <c:otherwise>
            Model not recognized
        </c:otherwise>
    </c:choose>
</table>
