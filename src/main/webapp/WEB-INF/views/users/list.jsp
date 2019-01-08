<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>

<tag:layout contentHeader="Users" title="Users">
    <jsp:attribute name="scripts"></jsp:attribute>

    <jsp:body>
        <div class="row">
            <div class="col-lg-12">
                <div class="box box-primary">
                    <div class="box-body">
                        <tag:users users="${users}" organizationId="${organizationId}" isNibss="${isNibss}" showButtons="true" />
                    </div>
                </div>

            </div>
        </div>
    </jsp:body>
</tag:layout>
