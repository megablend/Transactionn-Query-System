<%@tag language="java" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@attribute name="users" required="true" type="java.util.Collection" description="The collection of users to render" %>
<%@attribute name="showButtons" required="false" description="whether or not action buttons should be shown" type="java.lang.Boolean"  %>
<%@attribute name="isNibss" required="false" description="whether or not the organization is NIBSS" type="java.lang.Boolean"  %>
<%@attribute name="organizationId" required="true" description="whether or not the organization is NIBSS" type="java.lang.Integer"  %>



<c:url value="/resources/" var="resourceUrl" />
<div class="table-responsive">
    <table class="table table-bordered table-condensed table-striped myDataTable">
        <thead>
        <tr>
            <th style="width:20%">First Name</th>
            <th style="width:20%">Last Name</th>
            <th style="width:20%">Email</th>
            <th>Enabled?</th>
            <c:if test="${showButtons}">
                <script type="text/javascript">
                    <c:url value="/users/resetpassword" var="resetPasswordUrl" />
                    var ResetUserPasswordUrl = "${resetPasswordUrl}";

                    <c:url value="/users/updatestatus" var="updateUserStatusUrl" />
                    var UpdateUserStatusUrl = "${updateUserStatusUrl}";
                </script>
                <th style="text-align:right">
                     <div class="checkbox">
                         <label title="Check/Clear All">
                                   <input type="checkbox" id="chkAll"  class="flat-red"/>
                               </label>
                     </div>
                    <button class="btn btn-danger btn-flat btn-sm" title="Disable Users" id="btnDisableUsers">
                        <i class="fa fa-eye-slash" aria-hidden="true"></i>
                    </button>
                    <button class="btn btn-success btn-flat btn-sm" title="Enable Users" id="btnEnableUsers">
                        <i class="fa fa-eye" aria-hidden="true"></i>
                    </button>
                    <button class="btn btn-default btn-flat btn-sm" title="Reset User Passwords" id="btnResetPasswords">
                        <i class="fa fa-refresh" aria-hidden="true"></i>
                    </button>
                    <button class="btn btn-primary btn-flat btn-sm" title="Add New User" id="btnAddUser" data-toggle="modal" data-target="#usrCreationDialog">
                        <i class="fa fa-user-plus" aria-hidden="true"></i>
                    </button>
                </th>
            </c:if>
        </tr>
        </thead>
        <tbody>
        <c:if test="${users != null}">
            <c:forEach var="u" items="${users}">
                <tr>
                    <td>${u.firstName}</td>
                    <td>${u.lastName}</td>
                    <td>${u.email}</td>
                    <td>
                        <c:choose>
                            <c:when test="${u.enabled}">
                                <i class="fa fa-check" aria-hidden="true"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa fa-times" aria-hidden="true"></i>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <c:if test="${showButtons}">
                        <td>
                           <div class="checkbox">
                               <label>
                                   <input type="checkbox" id="${u.id}" name="chkUsers"  class="flat-red chkUsers"/>
                               </label>
                           </div>
                        </td>
                    </c:if>
                </tr>
            </c:forEach>
        </c:if>
        </tbody>
    </table>
</div>

<!-- dialog for user creation -->
<div class="modal fade" role="dialog" aria-labelledby="myLargeModalLabel" id="usrCreationDialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="gridSystemModalLabel">Add New User</h4>
            </div>
            <form class="form" name="userForm" id="userForm" method="post" action="<c:url value="/users" /> " autocomplete="off">
                <input type="hidden" name="organization" value="${organizationId}" />

                <div class="modal-body">
                    <div class="form-group">
                        <label for="firstName">First Name*</label>
                        <input type="text" name="firstName" id="firstName"  class="form-control"required />
                    </div>

                    <div class="form-group">
                        <label for="lastName">Last Name*</label>
                        <input type="text" name="lastName" id="lastName"  class="form-control"required />
                    </div>
                    <div class="form-group">
                        <label for="email">Email*</label>
                        <input type="text" name="email" id="email"  class="form-control"required />
                    </div>
                    <c:if test="${isNibss}">
                        <div class="form-group">
                            <label for="roles">Role*</label>
                        <%--    <select name="roles" id="roles" required class="form-control">
                                <option value="">--SELECT--</option>
                                <option value="ROLE_USER">User</option>
                                <option value="ROLE_ADMIN">Admin</option>
                            </select>--%>
                            <br/>
                            <label>
                                <input type="checkbox" name="roles" value="ROLE_USER" /> User
                            </label>
                            <br/>
                            <label>
                                <input type="checkbox" name="roles" value="ROLE_ADMIN" /> Admin
                            </label>
                            <br/>
                            <label>
                                <input type="checkbox" name="roles" value="ROLE_CL_USER" /> Corporate Lounge User
                            </label>
                            <br/>
                            <label>
                                <input type="checkbox" name="roles" value="ROLE_CL_ADMIN" />Corporate Lounge Admin
                            </label>
                        </div>
                    </c:if>

                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default btn-flat" data-dismiss="modal">
                        <i class="fa fa-times" aria-hidden="true"></i> Close</button>
                    <button type="submit" class="btn btn-primary btn-flat" id="btnSaveOrganization">
                        <i class="fa fa-bookmark" aria-hidden="true"></i> Save</button>
                </div>

            </form>
        </div>
    </div>
</div>
<script type="text/javascript" src="${resourceUrl}js/UserManagement.js"></script>