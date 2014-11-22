/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.web.mvc.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hoteia.qalingo.core.Constants;
import org.hoteia.qalingo.core.ModelConstants;
import org.hoteia.qalingo.core.domain.CatalogCategoryMaster_;
import org.hoteia.qalingo.core.domain.CatalogCategoryVirtualProductSkuRel_;
import org.hoteia.qalingo.core.domain.CatalogCategoryVirtual_;
import org.hoteia.qalingo.core.domain.ProductMarketing;
import org.hoteia.qalingo.core.domain.ProductMarketing_;
import org.hoteia.qalingo.core.domain.ProductSkuPrice_;
import org.hoteia.qalingo.core.domain.ProductSku_;
import org.hoteia.qalingo.core.fetchplan.FetchPlan;
import org.hoteia.qalingo.core.fetchplan.SpecificFetchMode;
import org.hoteia.qalingo.core.pojo.RequestData;
import org.hoteia.qalingo.core.service.ProductService;
import org.hoteia.qalingo.core.service.WebManagementService;
import org.hoteia.qalingo.core.solr.bean.ProductMarketingSolr;
import org.hoteia.qalingo.core.solr.response.ProductMarketingResponseBean;
import org.hoteia.qalingo.core.web.mvc.controller.AbstractFrontofficeQalingoController;
import org.hoteia.qalingo.core.web.mvc.viewbean.ProductMarketingViewBean;
import org.hoteia.qalingo.core.web.mvc.viewbean.RecentProductViewBean;
import org.hoteia.qalingo.web.mvc.factory.FormFactory;
import org.hoteia.qalingo.web.mvc.form.SearchForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.ui.Model;

/**
 * 
 * <p>
 * <a href="AbstractMCommerceController.java.html"><i>View Source</i></a>
 * </p>
 * 
 * @author Denis Gosset <a href="http://www.hoteia.com"><i>Hoteia.com</i></a>
 * 
 */
public abstract class AbstractMCommerceController extends AbstractFrontofficeQalingoController {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ProductService productMarketingService;

    @Autowired
    protected WebManagementService webManagementService;

    @Autowired
    protected FormFactory formFactory;
	
    protected List<SpecificFetchMode> productSkuFetchPlans = new ArrayList<SpecificFetchMode>();;
    protected List<SpecificFetchMode> productMarketingFetchPlans = new ArrayList<SpecificFetchMode>();
    protected List<SpecificFetchMode> categoryVirtualFetchPlans = new ArrayList<SpecificFetchMode>();;

    public AbstractMCommerceController() {
        productSkuFetchPlans.add(new SpecificFetchMode(ProductSku_.productMarketing.getName()));
        productSkuFetchPlans.add(new SpecificFetchMode(ProductSku_.attributes.getName()));
        productSkuFetchPlans.add(new SpecificFetchMode(ProductSku_.prices.getName()));
        productSkuFetchPlans.add(new SpecificFetchMode(ProductSku_.prices.getName() + "." + ProductSkuPrice_.currency.getName()));
        productSkuFetchPlans.add(new SpecificFetchMode(ProductSku_.assets.getName()));
        
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.productBrand.getName()));
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.productMarketingType.getName()));
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.attributes.getName()));
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.productSkus.getName()));
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.productSkus.getName() + "." + ProductSku_.prices.getName()));
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.productSkus.getName() + "." + ProductSku_.prices.getName() + "." + ProductSkuPrice_.currency.getName()));
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.productAssociationLinks.getName()));
        productMarketingFetchPlans.add(new SpecificFetchMode(ProductMarketing_.assets.getName()));
        
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.catalogCategories.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.parentCatalogCategory.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.attributes.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.categoryMaster.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.parentCatalogCategory.getName() + "." + CatalogCategoryVirtual_.categoryMaster.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.categoryMaster.getName() + "." + CatalogCategoryMaster_.catalogCategoryType.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.catalogCategoryProductSkuRels.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.catalogCategoryProductSkuRels.getName() + "." + CatalogCategoryVirtualProductSkuRel_.pk.getName() +  "." + org.hoteia.qalingo.core.domain.CatalogCategoryVirtualProductSkuPk_.productSku.getName()));
        categoryVirtualFetchPlans.add(new SpecificFetchMode(CatalogCategoryVirtual_.assets.getName()));
    }
    
    protected void loadRecentProducts(final RequestData requestData, final Model model){
        final HttpServletRequest request = requestData.getRequest();
        try {
            final List<String> listProductSkuCodes = requestUtil.getRecentProductSkuCodesFromCookie(request);
            List<RecentProductViewBean> recentProductViewBeans = frontofficeViewBeanFactory.buildListViewBeanRecentProduct(requestData, listProductSkuCodes, new FetchPlan(categoryVirtualFetchPlans), new FetchPlan(productMarketingFetchPlans), new FetchPlan(productSkuFetchPlans));
            model.addAttribute(ModelConstants.RECENT_PPRODUCT_MARKETING_VIEW_BEAN, recentProductViewBeans);
            
        } catch (Exception e) {
            logger.error("Can't load recent products", e);
        }
    }
    
    protected PagedListHolder<ProductMarketingViewBean> initList(final RequestData requestData, final String sessionKeyPagedListHolder, final ProductMarketingResponseBean productMarketingResponseBean,
                                                                 PagedListHolder<ProductMarketingViewBean> pagedListHolder, final SearchForm searchForm) throws Exception{
        int pageSize = searchForm.getPageSize();
        String sortBy = searchForm.getSortBy();
        String order = searchForm.getOrder();
        
        final List<ProductMarketingViewBean> productMarketingViewBeans = new ArrayList<ProductMarketingViewBean>();
        List<ProductMarketingSolr> searchtItems = productMarketingResponseBean.getProductMarketingSolrList();
        for (Iterator<ProductMarketingSolr> iterator = searchtItems.iterator(); iterator.hasNext();) {
            ProductMarketingSolr productMarketingSolr = (ProductMarketingSolr) iterator.next();
            ProductMarketing productMarketing = productMarketingService.getProductMarketingById(productMarketingSolr.getId(), new FetchPlan(productMarketingFetchPlans));
            ProductMarketingViewBean productMarketingViewBean = frontofficeViewBeanFactory.buildViewBeanProductMarketing(requestData, productMarketing);
            productMarketingViewBeans.add(productMarketingViewBean);
        }
        
        pagedListHolder = new PagedListHolder<ProductMarketingViewBean>(productMarketingViewBeans);
        pagedListHolder.setPageSize(pageSize);
        pagedListHolder.setSort(new MutableSortDefinition(sortBy, true, Constants.PAGE_ORDER_ASC.equalsIgnoreCase(order)));
        pagedListHolder.resort();
//        request.getSession().setAttribute(sessionKeyPagedListHolder, productsViewBeanPagedListHolder);
        return pagedListHolder;
    }
}