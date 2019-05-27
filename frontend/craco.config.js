const path = require('path');
const CracoLessPlugin = require('craco-less');
const argv = require('yargs').argv;
const BUILD_PATH = path.resolve(__dirname, './build');

const RemoveCssHashPlugin = {
    overrideWebpackConfig: ({webpackConfig, cracoConfig, pluginOptions, context: {env, paths}}) => {
        const plugins = webpackConfig.plugins;
        plugins.forEach(plugin => {

            const options = plugin.options;

            if (!options) {
                return;
            }

            if (options.filename && options.filename.endsWith('.css')) {
                options.filename = "static/css/[name].css";
            }

        });

        return webpackConfig;
    }
};

const RemoveJsHashPlugin = {
    overrideCracoConfig: ({cracoConfig, pluginOptions, context: {env, paths}}) => {
        cracoConfig.webpack = {
            configure:{
                optimization: {
                    splitChunks: {
                        cacheGroups: {
                            default: false,
                            vendors: false
                        },
                    },
                    runtimeChunk: false
                },
                output: {
                    path: BUILD_PATH,
                    filename: 'static/js/[name].js',
                },
            }
        };

        return cracoConfig
    }
};

const ConfigurableProxyTarget = {
    overrideCracoConfig: ({cracoConfig, pluginOptions, context: {env, paths}}) => {
        cracoConfig.devServer = (devServerConfig, { env, paths, proxy, allowedHost }) => {
            const proxyOverrides = Array.isArray(argv.proxy) ? argv.proxy : [ argv.proxy ].filter((override) => override);
            for (let i = 0; i < Math.min(proxyOverrides.length, devServerConfig.proxy.length); i++) {
                devServerConfig.proxy[i].target = proxyOverrides[i]
            }

            return devServerConfig;
        };

        return cracoConfig;
    }
};

module.exports = {
    plugins: [
        {plugin: CracoLessPlugin},
        {plugin: RemoveCssHashPlugin},
        {plugin: RemoveJsHashPlugin},
        {plugin: ConfigurableProxyTarget}
    ]
};