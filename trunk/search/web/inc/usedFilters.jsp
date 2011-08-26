<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<c:if test="${!empty param.q || param.f1 != null && param.f1 != '' || !empty paramValues.fq ||
              !empty param.issn || !empty param.title || !empty param.author || !empty param.rok || !empty param.keywords ||
              !empty param.udc ||!empty param.ddc || !empty param.onlyPublic || param.suggest=='true' }" >
<ul>
    <li style="border-color: #E66C00;border-right: 1px solid #E66C00;">
        <span class="ui-icon ui-icon-triangle-1-e folder"></span>
        <span style="text-indent:0px;width:auto;font-weight: bold;"><fmt:message bundle="${lctx}" key="filter.used" /></span><div class="clear"> </div>
        <ul id="usedFilters">
            <c:if test="${!empty param.q}" >
            <li>
                 <a class="mainNav" href="javascript:removeQuery();"><c:out value="${param.q}" /></a></li>
            </c:if>
            <%-- datum --%>
            <c:if test="${param.f1 != null && param.f1 != ''}">
                <li>
                <a title="" class="mainNav" href="javascript:removeDateAxisFilter();">
                <fmt:message bundle="${lctx}" key="common.date" />: <c:out value="${param.f1}" /> - <c:out value="${param.f2}" /></a></li>
            </c:if>

            <%-- filter queries --%>
            <c:forEach varStatus="status" var="fqs" items="${paramValues.fq}">
                    <c:set var="js">${fn:replace(fqs, "\"", "")}</c:set>
                    <c:set var="facetName">${fn:substringBefore(fqs,':')}</c:set>
                    <c:set var="facetName">${fn:replace(facetName, "\"", "")}</c:set>
                    <c:set var="facetValue"><c:out value="${fn:substringAfter(fqs,':')}" escapeXml="false" /></c:set>
                    <c:set var="facetValue">${fn:replace(facetValue, "\"", "")}</c:set>
                    <c:set var="facetValueDisp"><c:out value="${facetValue}" /></c:set>
                    <c:if test="${facetName == 'fedora.model' || facetName == 'document_type'}">
                        <c:set var="facetValueDisp"><fmt:message bundle="${lctx}" >fedora.model.<c:out value="${facetValueDisp}" /></fmt:message></c:set>
                    </c:if>
                    <c:if test="${facetName == 'dostupnost'}">
                        <c:set var="facetValueDisp"><fmt:message bundle="${lctx}" >dostupnost.<c:out value="${facetValueDisp}" /></fmt:message></c:set>
                    </c:if>
                    <li> <a  class="mainNav" href="javascript:removeFacet(<c:out value="${status.count}" />);">
                    <fmt:message bundle="${lctx}" ><c:out value="${facetName}" /></fmt:message>: <c:out value="${facetValueDisp}"/></a></li><input type="hidden" name="fq" id="fq<c:out value="${status.count}" />" value="<c:out value="${facetName}" />:<c:out value="${facetValue}" />" />

            </c:forEach>

            <%-- suggest params --%>
            <c:if test="${param.suggest=='true'}">
                    <c:set var="facetName"><c:out value="${fn:substringBefore(param.suggest_q,':')}" /></c:set>
                    <c:set var="facetName">${fn:replace(facetName, "\"", "")}</c:set>
                    <c:set var="facetValue"><c:out value="${fn:substringAfter(param.suggest_q,':')}" escapeXml="false" /></c:set>
                    <c:set var="facetValue">${fn:replace(facetValue, "\"", "")}</c:set>
                    <c:set var="facetValueDisp"><c:out value="${facetValue}" /></c:set>
                    <li> <a  class="mainNav" href="javascript:removeSuggest();">
                    <fmt:message bundle="${lctx}" key="Procházet" />: <fmt:message bundle="${lctx}" >suggest.<c:out value="${facetName}"/></fmt:message> - <c:out value="${facetValueDisp}"/>
                        </a></li><input type="hidden" name="suggest_q" id="suggest_q" value="<c:out value="${param.suggest_q}" />" />
                        <input type="hidden" name="suggest" id="suggest" value="true" />
            </c:if>

            <%-- advanced params --%>
            <c:if test="${!empty param.issn}">
                <li> <a title="" class="mainNav" href="javascript:removeAdvFilter('issn', '<c:out value="${param.issn}" />');">
                <fmt:message bundle="${lctx}" key="issn" />: <c:out value="${param.issn}"/></a></li>
            </c:if>
            <c:if test="${!empty param.title}">
                <li>
                <a title="" class="mainNav" href="javascript:removeAdvFilter('title', '<c:out value="${param.title}" />');">
                <fmt:message bundle="${lctx}">filter.maintitle</fmt:message>: <c:out value="${param.title}"/></a></li>
            </c:if>
            <c:if test="${!empty param.author}">
                <li>
                <a title="" class="mainNav" href="javascript:removeAdvFilter('author', '<c:out value="${param.author}" />');">
                <fmt:message bundle="${lctx}" key="author" /> &#160;<c:out value="${param.author}"/></a></li>
            </c:if>
            <c:if test="${!empty param.rok}">
                <li>
                <a title="" class="mainNav" href="javascript:removeAdvFilter('rok', '<c:out value="${param.rok}" />');">
                <fmt:message bundle="${lctx}" key="rok" />: &#160;<c:out value="${param.rok}"/></a></li>
            </c:if>
            <c:if test="${!empty param.keywords}">
                <li>
                <a title="" class="mainNav" href="javascript:removeAdvFilter('keywords', '<c:out value="${param.keywords}" />');">
                <fmt:message bundle="${lctx}" key="Keywords" />: &#160;<c:out value="${param.keywords}"/></a></li>
            </c:if>
            <c:if test="${!empty param.udc}">
                <li>
                <a title="" class="mainNav" href="javascript:removeAdvFilter('udc', '<c:out value="${param.udc}" />');">
                MDT: &#160;<c:out value="${param.udc}"/></a></li>
            </c:if>
            <c:if test="${!empty param.ddc}">
                <li>
                <a title="" class="mainNav" href="javascript:removeAdvFilter('ddc', '<c:out value="${param.ddc}" />');">
                DDT: &#160;<c:out value="${param.ddc}"/></a></li>
            </c:if>
            <c:if test="${!empty param.onlyPublic}">
                <li>
                <a title="" class="mainNav" href="javascript:removeAdvFilter('onlyPublic', '<c:out value="${param.onlyPublic}" />');">
                <fmt:message bundle="${lctx}" key="Pouze veřejné dokumenty" />:&#160; <c:out value="${param.onlyPublic}"/></a></li>
            </c:if>
        </ul>
    </li>
</ul>
<script type="text/javascript">
    function removeFacet(index){
        $('#fq'+index).remove();
        //checkQuery();
        $('#searchForm').submit();
    }
    
    function removeSuggest(){
        $('#suggest').remove();
        $('#suggest_q').remove();
        checkQuery();
        $('#searchForm').submit();
    }
    
    function removeDateAxisFilter(f1, f2){
        $('#f1').remove();
        $('#f2').remove();
        checkQuery();
        $('#searchForm').submit();
    }
    function removeQuery(){
    
        var page = new PageQuery(window.location.search);

        page.setValue("offset", "0");
        page.setValue("q", "");
        var url = "r.jsp?" + page.toString();
        window.location = url;
    }
    function removeAdvFilter(field, value){
        $('#'+field).val('');
        //checkQuery();
        $('#searchForm').submit();
    }
    
</script>
</c:if>

