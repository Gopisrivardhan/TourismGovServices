package com.tourismgov.site.service;

import java.util.List;

import com.tourismgov.site.dto.HeritageSiteRequest;
import com.tourismgov.site.dto.HeritageSiteResponse;

public interface HeritageSiteService {
    HeritageSiteResponse createSite(HeritageSiteRequest request);
    List<HeritageSiteResponse> getAllSites();
    HeritageSiteResponse getSiteById(Long siteId);
    HeritageSiteResponse updateSite(Long siteId, HeritageSiteRequest request);
    void deleteSite(Long siteId);
}