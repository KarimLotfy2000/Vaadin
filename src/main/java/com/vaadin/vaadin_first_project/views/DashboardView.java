package com.vaadin.vaadin_first_project.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.vaadin_first_project.data.Service.CrmService;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {
    private CrmService service;
    public DashboardView(CrmService service) {
        this.service = service;
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        addClassName("dashboard-view");
        setSizeFull();
        add(getContactStats(),getCompaniesCharts());

     }
    private Component getContactStats() {
        Span stats = new Span("Total Contacts: " + service.countContacts());
        stats.addClassNames("text-xl", "font-bold");
        return stats;
    };
    private Component getCompaniesCharts() {
        Span chart = new Span("Companies Chart Placeholder");
        chart.addClassNames("text-l", "font-medium");
        return chart;
    };
}





