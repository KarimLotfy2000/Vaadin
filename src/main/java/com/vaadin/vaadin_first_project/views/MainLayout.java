package com.vaadin.vaadin_first_project.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
 import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.vaadin_first_project.views.components.list.ListView;


public class MainLayout extends AppLayout {
    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 title = new H1("CRM Application");
        title.addClassNames("text-m", "m-m","font-bold","color-primary");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(),title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.expand(title);
        header.addClassNames("py-0","px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink listLink = new RouterLink("Contacts", ListView.class);
        listLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        dashboardLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink excelLink = new RouterLink("Excel Editor", ExcelView.class);
        excelLink.setHighlightCondition(HighlightConditions.sameLocation());

        var links = new VerticalLayout(listLink,dashboardLink,excelLink);
         links.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
         links.setSizeFull();


        addToDrawer(links);
    }

}
