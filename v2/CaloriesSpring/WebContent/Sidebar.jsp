

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
				<sec:authorize access="hasRole('ROLE_ADMIN')">
                <li>
                    <a href="userEdit.jsp">Users</a>
                </li>
                </sec:authorize>
                <li>
                <br/>
                </li>
                <li>
	                <a href="logout">Logout</a>
                </li>
            </ul>
        </div>
        <!-- /#sidebar-wrapper -->
