<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tag:layout contentHeader="Resource Not Found" title="Resource Not Found">
    <jsp:attribute name="scripts"></jsp:attribute>
    <jsp:body>

        <div class="row">
            <div class="col-lg-12">
                <div class="box">
                    <div class="box-body">
                        <div class="bg-warning">
                            <h3 style="font-size: 1.5em;"><i class="fa fa-warning"></i> The requested resource could not be located.</h3>
                        </div>
                        <sec:authorize access="isAuthenticated()">
                            <div style="text-align:center">
                                <a class="btn btn-lg btn-primary" href="<c:url value="/" /> ">
                                    <i class="fa fa-home"></i> Return to Home Page
                                </a>
                            </div>
                        </sec:authorize>
                        <sec:authorize access="isAnonymous()">
                            <a class="btn btn-lg btn-primary" href="<c:url value="/" /> ">
                                <i class="fa fa-home"></i> Go to Login Page
                            </a>
                        </sec:authorize>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
 </tag:layout>