# How to Deploy Workshops to GitHub Pages

Learn how to export and deploy workshops to GitHub Pages for free static hosting while maintaining an upgrade path to enhanced OpenShift features.

## 🎯 When to Use GitHub Pages

GitHub Pages deployment is ideal for:
- **Wide Distribution**: Free hosting accessible to everyone
- **Cost-Conscious Projects**: No infrastructure costs
- **Pilot Programs**: Test workshop reception before investing in enhanced features
- **Simple Workshops**: Content-focused workshops without need for real-time assistance
- **Offline Access**: Participants can download and use offline

## 📋 Prerequisites

- Workshop created using the Workshop Template System
- GitHub repository (public for free GitHub Pages)
- Source Manager Agent running (Port 10060)

## 🚀 Step-by-Step Export Process

### Step 1: Export Workshop for GitHub Pages

Use the Source Manager Agent to export your workshop:

```bash
# Export workshop for GitHub Pages
curl -X POST http://localhost:10060/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "export-001",
    "params": {
      "id": "export-001",
      "sessionId": "github-pages-export",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Export healthcare-ml-workshop for GitHub Pages deployment with upgrade information"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Step 2: Create GitHub Repository

1. **Create New Repository**:
   - Go to GitHub.com → New Repository
   - Name: `healthcare-ml-workshop` (or your workshop name)
   - Visibility: Public (required for free GitHub Pages)
   - Initialize with README: No (we'll add our own)

2. **Clone Repository Locally**:
   ```bash
   git clone https://github.com/yourusername/healthcare-ml-workshop.git
   cd healthcare-ml-workshop
   ```

### Step 3: Upload Exported Content

```bash
# Copy exported files to repository
cp -r ./github-pages-export/* ./healthcare-ml-workshop/

# Add and commit files
cd healthcare-ml-workshop
git add .
git commit -m "Deploy Healthcare ML workshop to GitHub Pages"
git push origin main
```

### Step 4: Enable GitHub Pages

1. **Go to Repository Settings**:
   - Navigate to your repository on GitHub
   - Click "Settings" tab
   - Scroll down to "Pages" section

2. **Configure GitHub Pages**:
   - Source: "Deploy from a branch"
   - Branch: `main`
   - Folder: `/ (root)`
   - Click "Save"

3. **Wait for Deployment**:
   - GitHub will build and deploy your site
   - Process takes 5-10 minutes
   - You'll receive an email when complete

### Step 5: Access Your Workshop

Your workshop will be available at:
```
https://yourusername.github.io/healthcare-ml-workshop
```

## 📊 Feature Comparison

### ✅ Available in GitHub Pages Version

**Complete Workshop Content**:
- All workshop modules and exercises
- Professional Showroom styling and branding
- Static navigation and search functionality
- Downloadable resources and materials

**User Experience**:
- Mobile-responsive design
- Offline accessibility (can be downloaded)
- Fast loading (static content)
- SEO-optimized for discoverability

**Help and Support**:
- Static troubleshooting guides
- FAQ sections
- Contact information for support
- Links to additional resources

### 🚀 Enhanced Features (OpenShift Only)

**AI-Powered Assistance**:
- Real-time Workshop Chat Agent
- Context-aware question answering
- Dynamic navigation assistance
- Personalized learning guidance

**Live Content Management**:
- Automatic external documentation updates
- Real-time content validation
- Dynamic knowledge base integration
- Continuous monitoring and improvements

**Advanced Analytics**:
- Participant progress tracking
- Engagement metrics and insights
- Learning outcome assessment
- Usage pattern analysis

## 🔄 Upgrade Path to OpenShift

### When to Consider Upgrading

**Upgrade Indicators**:
- Participants asking complex, context-specific questions
- Need for real-time assistance during workshops
- Requirement for always-current documentation
- Desire for participant analytics and insights
- Workshop complexity requiring dynamic support

### Upgrade Process

1. **Deploy to OpenShift**:
   ```bash
   # Use Source Manager Agent for OpenShift deployment
   curl -X POST http://localhost:10060/send-task \
     -H "Content-Type: application/json" \
     -d '{
       "message": {
         "parts": [{
           "text": "Deploy healthcare-ml-workshop to OpenShift with full agent support"
         }]
       }
     }'
   ```

2. **Configure Enhanced Features**:
   - Set up all 6 workshop agents
   - Configure Pinecone for RAG functionality
   - Enable external documentation monitoring
   - Set up participant analytics

3. **Seamless Migration**:
   - Same workshop content and styling
   - Enhanced with AI and dynamic features
   - Participants experience upgrade transparently

### Cost Comparison

| Feature | GitHub Pages | OpenShift |
|---------|-------------|-----------|
| **Hosting** | Free | Infrastructure costs |
| **AI Chat Agent** | ❌ | ✅ |
| **Live Updates** | ❌ | ✅ |
| **Analytics** | Basic | Advanced |
| **Support** | Static | Real-time |
| **Maintenance** | Manual | Automated |

## 🔧 Customization Options

### Branding and Styling

The exported workshop maintains professional Showroom styling but can be customized:

```css
/* Custom CSS for GitHub Pages version */
.workshop-header {
    background-color: #your-brand-color;
}

.upgrade-banner {
    background: linear-gradient(45deg, #ee7724, #d8363a);
    color: white;
    padding: 10px;
    text-align: center;
}
```

### Adding Custom Features

```html
<!-- Add custom JavaScript for GitHub Pages -->
<script>
// Custom analytics (Google Analytics, etc.)
gtag('config', 'GA_MEASUREMENT_ID');

// Custom help widget
function showCustomHelp() {
    // Your custom help implementation
}
</script>
```

### Domain Configuration

For custom domains:

1. **Add CNAME file**:
   ```bash
   echo "workshop.yourcompany.com" > CNAME
   git add CNAME
   git commit -m "Add custom domain"
   git push
   ```

2. **Configure DNS**:
   - Add CNAME record pointing to `yourusername.github.io`
   - Wait for DNS propagation (up to 24 hours)

## 📈 Success Metrics

### GitHub Pages Deployment Success

**Technical Metrics**:
- ✅ Site loads successfully at GitHub Pages URL
- ✅ All workshop modules accessible
- ✅ Navigation works correctly
- ✅ Mobile-responsive design functions
- ✅ Static search functionality works

**User Experience Metrics**:
- ✅ Professional appearance maintained
- ✅ Content readable and well-formatted
- ✅ Help resources easily accessible
- ✅ Download links functional
- ✅ Contact information available

### Upgrade Decision Metrics

**Monitor These Indicators**:
- Participant questions complexity
- Support request frequency
- Workshop completion rates
- Feedback about needing real-time help
- Requests for current documentation

## 🎯 Best Practices

### Content Optimization

1. **Static-Friendly Content**:
   - Ensure all links work in static environment
   - Include comprehensive troubleshooting guides
   - Provide downloadable resources
   - Add clear contact information

2. **Performance Optimization**:
   - Optimize images for web
   - Minimize CSS and JavaScript
   - Use efficient file formats
   - Enable browser caching

### Maintenance Strategy

1. **Regular Updates**:
   - Schedule periodic content reviews
   - Update external links and references
   - Refresh screenshots and examples
   - Validate all download links

2. **Feedback Collection**:
   - Add feedback forms or links
   - Monitor GitHub Issues for problems
   - Collect participant suggestions
   - Track upgrade interest

## 🔗 Related Documentation

- [Repository-Based Workshops Tutorial](../tutorials/repository-based-workshops.md)
- [OpenShift Deployment Guide](../reference/deployment.md)
- [Source Manager Agent Reference](../reference/agent-api.md#source-manager-agent)
- [Configuration Examples](../reference/configuration.md)

---

*GitHub Pages deployment provides free, accessible workshop hosting with a clear upgrade path to enhanced OpenShift features when needed.*
