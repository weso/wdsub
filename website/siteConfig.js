const repoUrl = "https://github.com/weso/wdsub";

const apiUrl = "/wdsub/api/es/weso/index.html";

// See https://docusaurus.io/docs/site-config for available options.
const siteConfig = {
  title: "wdsub",
  tagline: "wdsub",
  url: "https://weso.github.io/wdsub",
  baseUrl: "/wdsub/",

  customDocsPath: "wdsub-docs/target/mdoc",

  projectName: "wdsub",
  organizationName: "weso",

  headerLinks: [
    { href: apiUrl, label: "API Docs" },
    { doc: "overview", label: "Documentation" },
    { href: repoUrl, label: "GitHub" }
  ],

  headerIcon: "img/logo-weso.png",
  titleIcon: "img/logo-weso.png",
  favicon: "img/favicon/favicon.ico",

  colors: {
    primaryColor: "#122932",
    secondaryColor: "#153243"
  },

  copyright: `Copyright © 2019-${new Date().getFullYear()} WESO Research group.`,

  highlight: { theme: "github" },

  onPageNav: "separate",

  separateCss: ["api"],

  cleanUrl: true,

  repoUrl,

  apiUrl
};

module.exports = siteConfig;
