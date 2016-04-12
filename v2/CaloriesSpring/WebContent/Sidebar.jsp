

        <!-- Sidebar -->
        <div id="sidebar-wrapper">
            <ul class="sidebar-nav">
                <li class="sidebar-brand">
                    <a href="index.jsp">
                        Meals Manager
                    </a>
                </li>
                <!--  no need to secure this as it will be available to any authenticated users -->
                <li>
                    <a href="calendarCalories.jsp">Meals Calendar</a>
                </li>
                <li>
                    <a href="userEdit.jsp">User Management</a>
                </li>
                <li>
                <br/>
                </li>
                <li>
	                <a href="logout">Logout &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(<sec:authentication property="principal.login" />)</a>
                </li>
            </ul>
        </div>
        <!-- /#sidebar-wrapper -->
