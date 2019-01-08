<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<tag:layout contentHeader="Exception" title="Exception" >
    <jsp:attribute name="scripts"></jsp:attribute>
    <jsp:body>
        <div class="row">

            <div class="col-lg-12">
                <div class="box">
                    <div class="box-body">
                        <div>
                            <div class="bg-danger">
                                <h3 style="font-size: 1.3em;"><i class="fa fa-warning"></i> </h3>
                                <p>Sorry, an exception occurred while processing your request.</p>
                            </div>
                            <sec:authorize access="isAuthenticated()">
                               <div style="text-align:center">
                                   <a class="btn btn-lg btn-primary" href="<c:url value="/" /> ">
                                       <i class="fa fa-home"></i> Return to Home Page
                                   </a>
                               </div>
                            </sec:authorize>
                            <div class="table-responsive">
                                <table class="table table-condensed table-bordered table-striped">
                                    <tr>
                                        <td style="width:30%">Error</td>
                                        <td><pre>${error}</pre></td>
                                    </tr>
                                    <tr>
                                        <td>Message</td>
                                        <td><pre>${message}</pre></td>
                                    </tr>
                                    <tr>
                                        <td>Timestamp</td>
                                        <td><pre>${timestamp}</pre></td>
                                    </tr>
                                    <tr>
                                        <td>Trace</td>
                                        <td><pre>${trace}</pre></td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
 </tag:layout>
