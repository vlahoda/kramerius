<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*" %>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager, cz.incad.kramerius.processes.DefinitionManager"%>

<%
    if(pageContext.getAttribute("lctx")==null){
        Injector inj2 = (Injector)application.getAttribute(Injector.class.getName());
        pageContext.setAttribute("lrProcessManager",inj2.getInstance(LRProcessManager.class));
        pageContext.setAttribute("dfManager",inj2.getInstance(DefinitionManager.class));

        LocalizationContext lctx2= inj2.getProvider(LocalizationContext.class).get();
        pageContext.setAttribute("lctx", lctx2);
    }
%>


<%--
Get Biblio mods
<c:set var="models" value="${fn:split(param.path, '/')}"/>
<c:set var="pids" value="${fn:split(param.pid_path, '/')}"/>
--%>
<c:set var="lastModel" value="${models[fn:length(models)-1]}" />
<c:set var="model_path" value="itemTree" scope="request" />
<c:set var="href" value="#{href}" />
<c:set var="label" value="#{label}" />
<c:set var="level" value="0"/>
<c:if test="${!empty param.level}" >
    <c:set var="level" value="${param.level}"/>
</c:if>
<%-- fill path up to the end --%>
<%
	ArrayList<String> pids =  new ArrayList<String>(Arrays.asList((String [])request.getParameter("pid_path").split("/")));
	ArrayList<String> models =  new ArrayList<String>(Arrays.asList((String [])request.getParameter("path").split("/")));
	FedoraUtils.fillFirstPagePid(pids, models);
        for(String pid:pids){
            System.out.println(pid);
        }
	getServletContext().setAttribute("pids", pids);
	getServletContext().setAttribute("models", models);
	getServletContext().setAttribute("pathsize", models.size());
	
	//imagePid = pids.get(pids.size()-1);
%>
<c:forEach var="uuid" varStatus="status" items="${pids}">
    <c:choose>
        <c:when test="${level==0 || status.count>1}">
    <c:set var="cur_level" value="${status.count + level}"/>
    <c:set var="obj" value="#tabs_${cur_level}"/>
    <script language="javascript">
        $(document).ready(function(){
            var obj = "<c:out value="${obj}" />";
            var tabTemp = '<li><a href="<c:out value="${href}" />"><c:out value="${label}" /></a><img width="12px" src="img/empty.gif" class="op_list" onclick="showList(this, \''+obj+'\', \'<c:out value="${href}" />\')" /></li>';
            $(obj).tabs({ tabTemplate: tabTemp });
            
    <%--
           getItemRels('<c:out value="${pids[status.count-1]}" />', '<c:out value="${pids[status.count]}" />', <c:out value="${cur_level}" />, <c:out value="${status.count == fn:length(models)}" />);
    --%>
        });
    </script>
    <div id="tabs_<c:out value="${cur_level}" />" style="padding:2px;" pid="<c:out value="${uuid}" />">
        <ul><li><a href="#tab<c:out value="${status.count}" />-<c:out value="${models[status.count -1]}" />" ><fmt:message bundle="${lctx}">fedora.model.<c:out value="${models[status.count -1]}" /></fmt:message>
        </a><img width="12px" src="img/empty.gif" class="op_list" onclick="showList(this, '#tabs_<c:out value="${cur_level}" />', '<c:out value="${models[status.count -1]}" />')" /></li></ul>
        <div id="tab<c:out value="${cur_level}" />-<c:out value="${models[status.count -1]}" />" class="<c:out value="${models[status.count -1]}" />" >
    <jsp:useBean id="uuid" type="java.lang.String" />
    <c:set var="urlStr" >
        <c:out value="${kconfig.fedoraHost}" />/get/uuid:<c:out value="${uuid}" />/BIBLIO_MODS
    </c:set>
    <c:set var="display" value="none"/>
    
    <c:catch var="exceptions"> 
        <c:import url="${urlStr}" var="xml2" charEncoding="UTF-8"  />
        <c:import url="inc/details/xsl/default.jsp?model=${models[status.count -1]}&display=${display}&language=${param.language}${others}" var="xslt" charEncoding="UTF-8"  />
    </c:catch>
    <c:choose>
        <c:when test="${exceptions != null}" >
            <c:out value="${xml2}" />
            <c:out value="${xslt}" />
            <c:out value="${exceptions}" />
        </c:when>
        <c:otherwise>
            <div class="relList" style="display:none;" id="list-<c:out value="${models[status.count -1]}" />"></div>
            <%@ include file="../../admin/itemOptions.jsp" %>
            <div id="info-<c:out value="${models[status.count -1]}" />" style="min-height: 16px;">
            <x:transform doc="${xml2}"  xslt="${xslt}"  >
                <x:param name="pid" value="${uuid}"/>
            </x:transform></div>
        </c:otherwise>
    </c:choose>
    
            
        </c:when>
    </c:choose>
</c:forEach>
<c:forEach var="model" varStatus="status" items="${models}">
    <c:choose>
        <c:when test="${level==0 || status.count>1}">
    </div></div>
            
        </c:when>
    </c:choose>
</c:forEach>

<script language="javascript">
    $(document).ready(function(){
        $('#tabs_1>ul>li>img.op_list').hide();
       getItemRels('<c:out value="${pids[0]}" />', '<c:out value="${pids[1]}" />', <c:out value="${1 + level}" />, true);
       changeSelection('<c:out value="${pids[pathsize -2]}" />','<c:out value="${pids[pathsize -1]}" />');
        
    });
        
    initParent = '<c:out value="${pids[pathsize -2]}" />';
    initPage = '<c:out value="${pids[pathsize -1]}" />';
    function startPage(){
        currentSelectedPage = initPage;
        selectPage(initPage);
    }
    
    function startItemMenu(){
        $('.item_options').show();
        $('.menu_activation').unbind('mouseenter');
        $('.item_options').unbind('mouseleave');
        
        $('.item_options').each(function(){
            var l = $(this).parent().width() + $($(this).parent()).offset().left - 9;
           $(this).css('left',l);
        });
        $('.menu_activation').bind('click', function(){
            $('.item_options').stop();
           var il = $(this).parent().parent().width() + $(this).parent().parent().offset().left - 9;
           if($(this).parent().offset().left == il){
               $(this).parent().css({'width': 129, 'left': '-=120'});
           }
        });
        
        $('.item_options').bind('mouseleave', function(){
            $('.menu_activation').stop();
           var il = $(this).parent().width() + $(this).parent().offset().left -9;
           var fl = il - 120;
           if($(this).offset().left == fl){
               $(this).animate({
                   width: 9,
                   left: '+=120'
               }, 50);
           }else{
               $(this).css('left',il);
               $(this).css('width',9);
           }
        });
    }
    
</script>
