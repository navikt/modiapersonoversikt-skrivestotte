const CracoLessPlugin = require('craco-less');
const { ConfigurableProxyTarget, ChangeJsFilename, ChangeCssFilename} = require('@navikt/craco-plugins');

module.exports = {
    plugins: [
        {plugin: CracoLessPlugin},
        {plugin: ChangeCssFilename},
        {plugin: ChangeJsFilename},
        {plugin: ConfigurableProxyTarget}
    ]
};