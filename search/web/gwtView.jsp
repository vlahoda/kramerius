<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>
<script type="text/javascript" language="javascript" src="gwtviewers/gwtviewers.nocache.js"></script>
 
<script type="text/javascript">
	var __gwtViewersUUID = "uuid:${param.pid}";

	var __confHeight = 125;
	var __confWidth = 400;
	var __confDistance = 5;

	var __confNumberOfImages = 7;	
	var __confMoveStep = 1;
        
	function selectPage(uuid){
	    alert("selectuju " + uuid);
	}

	function pages(from, to) {
	}
	
</script>
 
<table align="center">
	<tr>
		<td colspan="3" id="label"></td>
	</tr>
	<tr>
		<td id="container"></td>
	</tr>
	<tr>
		<td><div id="slider" style="width: 100%"></div> </td>
	</tr>
</table>




